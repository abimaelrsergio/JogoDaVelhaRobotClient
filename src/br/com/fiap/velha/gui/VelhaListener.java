package br.com.fiap.velha.gui;

/**
 * Interface que define os metodos para receber eventos de GUI e de Rede. 
 */
public interface VelhaListener {
	
	/**
	 * Metodo executado quando o tabuleiro e' clicado.
	 * @param posicao Posicao no tabuleiro (0..8)
	 */
	public void onClickTabuleiro(int posicao);
	
	/**
	 * Metodo executado quando uma string e' recebida pelo socket de rede.
	 * @param message String recebida pelo socket
	 */
	public void onMessageReceived(String message);

	/**
	 * Metodo executado quando uma conexao com o servidor de rede e' aberta.
	 */
	public void onSessionOpened();

	/**
	 * Metodo executado quando uma conexao com o servidor de rede e' fechada.
	 */
	public void onSessionClosed();

	/**
	 * Metodo executado quando o botao "Conectar" e' clicado.
	 * @param hostName Hostname do servidor (ou endereco IP)
	 * @param tcpPort Porta TCP
	 */
	public void onClickConnect(String hostName, Integer tcpPort);

	/**
	 * Metodo executado quando o botao "Abortar" e' clicado.
	 */
	public void onClickAbort();

}
