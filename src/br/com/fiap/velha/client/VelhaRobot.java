package br.com.fiap.velha.client;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import br.com.fiap.velha.VelhaBean;
import br.com.fiap.velha.VelhaEngine;
import br.com.fiap.velha.VelhaParser;
import br.com.fiap.velha.gui.VelhaListener;
import br.com.fiap.velha.gui.VelhaRobotGUI;

/**
 * Classe que implementa um Cliente Robot de Jogo da Velha. 
 */
public class VelhaRobot implements VelhaListener {

	/** Timeout de conexao, em ms. */
	private static final long CONNECTOR_TIMEOUT = 30000L;
	/** Tamanho do buffer da sessao, em bytes. */
	private static final int CONNECTOR_BUFFER_SIZE = 2048;
	/** Intervalo de tempo para reconexao ao servidor, em ms. */
	private static final int RECONNECT_DELAY = 500;
	/** Intervalo de tempo para realizar uma jogada automatica, em ms. */
	private static final int JOGADA_DELAY = 1000;
	
	/** Logica do Jogo da Velha. */
	private VelhaEngine  velhaEngine  = null;
	/** Parser de XML do Jogo da Velha. */
	private VelhaParser  velhaParser  = null;
	/** Bean (POJO) com a representacao do XML do Jogo da Velha. */
	private VelhaBean    velhaBean    = null;
	/** Manipulador de sockets do cliente do Jogo da Velha. */
	private VelhaHandler velhaHandler = null;
	/** GUI do cliente do Jogo da Velha. */
	private VelhaRobotGUI     velhaGUI     = null;

	/** Connector do client de sockets. */ 
	private NioSocketConnector velhaSockConnector = null;
	/** Objeto com a conexao de socket. */
	private ConnectFuture      velhaConnect       = null;
	
	/** Indica se deve reconectar automaticamente ao servidor apos a partida. */
	private boolean reconectar = false;
	/** Nome do host ou endereco IP do servidor. */
	private String hostName = null;
	/** Numero da Porta TCP do servidor. */
	private int tcpPort = -1;
	
	/**
	 * Metodo principal de execucao do programa.
	 * @param args Argumentos de linha de comando
	 */
	public static void main(String[] args) {
		new VelhaRobot();
	}

	/** Construtor default. */
	public VelhaRobot() {
		velhaEngine    = new VelhaEngine();
		velhaParser    = new VelhaParser();
		velhaBean      = new VelhaBean();
		velhaHandler   = new VelhaHandler(this);
		velhaGUI       = new VelhaRobotGUI(velhaEngine, this);
	}

	/**
	 * Metodo executado quando o tabuleiro e' clicado.
	 * @param posicao Posicao no tabuleiro (0..8)
	 */
	@Override
	public void onClickTabuleiro(int posicao) {
		/* robot nao aceita clicks */
	}

	/**
	 * Metodo executado quando uma string e' recebida pelo socket de rede.
	 * @param message String recebida pelo socket
	 */
	@Override
	public void onMessageReceived(String message) {
		/* processa xml recebido */
		processarXML(message);
		/* delay para "parecer humano" */
		try {
			Thread.sleep(JOGADA_DELAY);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		/* obtem jogada automatica do robot */
		int posicao = velhaEngine.getJogada(velhaBean.getId());
		/* registra jogada e envia xml */
		if (posicao >= 0) registrarJogada(posicao);
	}

	/**
	 * Metodo executado quando uma conexao com o servidor de rede e' aberta.
	 */
	@Override
	public void onSessionOpened() {	}

	/**
	 * Metodo executado quando uma conexao com o servidor de rede e' fechada.
	 */
	@Override
	public void onSessionClosed() {
		
		/* limpa o status do jogo */
		limparStatusJogo();
		
		/* reconecta */
		if (reconectar) {
			try {
				Thread.sleep(RECONNECT_DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			conectar(this.hostName, this.tcpPort);
		}
	}

	/**
	 * Metodo executado quando o botao "Conectar" e' clicado.
	 * @param hostName Hostname do servidor (ou endereco IP)
	 * @param tcpPort Porta TCP
	 */
	@Override
	public void onClickConnect(String hostName, Integer tcpPort) {

		this.hostName = hostName;
		this.tcpPort  = tcpPort;
		reconectar    = true;
		conectar(this.hostName, this.tcpPort);
		
		/* habilita/desabilita controles no painel */
		velhaGUI.habilitarPainelConexao(false);
		velhaGUI.habilitarBotaoAbortar(true);
	}

	/**
	 * Metodo executado quando o botao "Abortar" e' clicado.
	 */
	@Override
	public void onClickAbort() {
		
		reconectar = false;
		desconectar();
		
		/* habilita/desabilita controles no painel */
		velhaGUI.habilitarPainelConexao(true);
		velhaGUI.habilitarBotaoAbortar(false);
	}

	/**
	 * Registra uma jogada no tabuleiro e envia um XML ao servidor.
	 * @param posicao Posicao da jogada
	 */
	private void registrarJogada(int posicao) {
		/* se jogada e' invalida, sai */
		if ((velhaEngine.getPosicao(posicao) != VelhaEngine.JOGADOR_VAZIO) ||
			(!velhaBean.getStatus().equals(VelhaEngine.STATUS_JOGUE)) ||
			(velhaEngine.isGameOver())) return;

		/* coloca jogada no tabuleiro */
		velhaEngine.setPosicao(posicao, velhaBean.getId());
		/* coloca jogada no bean */
		velhaBean.setJogada(posicao);
		/* envia xml */
		velhaHandler.sendMessage(velhaParser.getVelhaXML(velhaBean));
	}

	/**
	 * Processa um XML recebido, atualizando o status do jogo e do tabuleiro.
	 * @param message Mensagem recebida, em formato XML.
	 */
	private void processarXML(String message) {
		/* converte xml para o objeto VelhaBean */
		VelhaBean tempBean = velhaParser.getVelhaBean(message);
		
		/* atualiza estado do jogo e tabuleiro */
		if (tempBean != null) {
			velhaBean = tempBean;
			
			velhaEngine.copiarTabuleiro(velhaBean.getTabuleiro());
			velhaGUI.redesenharTabuleiro();
			
			velhaGUI.escreverInfo(getStatusInfo());
		}
	}
	
	/**
	 * Retorna mensagem de informacao baseado no status do jogo, armazenado no
	 *   objeto VelhaBean
	 * @return String com a mensagem.
	 */
	private String getStatusInfo() {
		/* obtem id do jogador (X ou O) */
		String mensagem = "Jogador " + String.valueOf(velhaBean.getId());

		/* retorna mensagem conforme status do jogo */
		if (VelhaEngine.STATUS_AGUARDE.equals(velhaBean.getStatus())) {
			mensagem += ": Aguarde o oponente...";
		}
		if (VelhaEngine.STATUS_JOGUE.equals(velhaBean.getStatus())) {
			mensagem += ": Sua vez! Jogue!";
		}
		if (VelhaEngine.STATUS_GANHOU.equals(velhaBean.getStatus())) {
			mensagem += ": Parabens! Voce ganhou o jogo!";
		}
		if (VelhaEngine.STATUS_PERDEU.equals(velhaBean.getStatus())) {
			mensagem += ": Sinto muito, voce perdeu. Treine um pouco mais!";
		}
		if (VelhaEngine.STATUS_EMPATE.equals(velhaBean.getStatus())) {
			mensagem += ": Houve empate!";
		}
		if (VelhaEngine.STATUS_WO.equals(velhaBean.getStatus())) {
			mensagem += ": O oponente abandonou o jogo.";
		}
		return mensagem;
	}

	/**
	 * Conecta no servidor de sockets do Jogo da Velha.
	 * @param hostName Nome do host ou numero IP do servidor
	 * @param tcpPort Numero da Porta TCP do servidor
	 */
	private void conectar(String hostName, Integer tcpPort) {
		
		/* cria conector */
		velhaSockConnector = new NioSocketConnector();
		/* configura conector */
		velhaSockConnector.getFilterChain().addLast(
				"logger", new LoggingFilter());
		velhaSockConnector.getFilterChain().addLast(
				"codec", new ProtocolCodecFilter(
						new TextLineCodecFactory(Charset.forName("UTF-8"))));
		velhaSockConnector.setConnectTimeoutMillis(CONNECTOR_TIMEOUT);
		velhaSockConnector.setHandler(velhaHandler);
		
		/* conecta ao servidor e configura sessao */
		velhaConnect = velhaSockConnector.connect(
				new InetSocketAddress(hostName, tcpPort));
		velhaSockConnector.getSessionConfig().setReadBufferSize(
				CONNECTOR_BUFFER_SIZE);
		velhaConnect.awaitUninterruptibly();
	}

	/** Desconecta do servidor. */
	private void desconectar() {
		
		/* fecha sessao */
		if (velhaConnect.isConnected()
				&& velhaConnect.getSession().isConnected()) { 
			velhaConnect.getSession().close(true);
		}
		/* libera da memoria os recursos do conector */
		velhaSockConnector.dispose();
		
		/* limpa o status do jogo */
		limparStatusJogo();
	}

	/** Limpa o status do jogo. */
	private void limparStatusJogo() {
		
		velhaGUI.escreverInfo("Desconectado.");
		velhaEngine.limparTabuleiro();
		velhaBean.limpar();
		velhaGUI.redesenharTabuleiro();
	}
}
