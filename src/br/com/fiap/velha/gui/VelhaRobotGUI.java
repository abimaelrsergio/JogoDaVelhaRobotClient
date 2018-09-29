package br.com.fiap.velha.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EtchedBorder;

import br.com.fiap.velha.VelhaEngine;

/**
 * Classe responsavel por montar a GUI em Swing do Jogo da Velha. 
 */
public class VelhaRobotGUI implements ActionListener {

	/** Nome do "look and feel" usado pela aplicacao. */
	private static final String LOOK_AND_FEEL_NAME = "Nimbus";
	/** Hostname default. */
	private static final String HOST_NAME_DEFAULT = "localhost";
	/** Porta TCP default. */
	private static final int TCP_PORT_DEFAULT = 9123;
	
	/* frame da janela principal */
	private JFrame     velhaFrame     = null;

	/* painel do tabuleiro */
	private VelhaPanel velhaPanel     = null;

	/* painel de conexao */
	private JPanel     conexaoPanel   = null;
	private JTextField hostName       = null;
	private JSpinner   tcpPort        = null;
	private JButton    connectButton  = null;
	private JButton    abortButton    = null;
	private JSpinner   nivelRobot     = null;
	
	/* painel de informacao */
	private JPanel     infoPanel      = null;
	private JLabel     infoLabel      = null;
	
	/** Armazena uma referencia para um objeto VelhaListener. */
	private VelhaListener velhaListener = null;
	/** Armazena uma referencia para um objeto VelhaEngine. */
	private VelhaEngine   velhaEngine   = null;
	
	/**
	 * Construtor.
	 * @param engine Referencia para objeto VelhaEngine
	 * @param listener Referencia para objeto VelhaListener
	 */
	public VelhaRobotGUI(VelhaEngine engine, VelhaListener listener) {

		velhaListener = listener;
		velhaEngine   = engine;
		
		setLookAndFeel();
		
		velhaFrame = new JFrame("Robot do Jogo da Velha");
		velhaFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		velhaFrame.setLayout(new BorderLayout(10, 10));
		
		criarPainelConexao();
		criarPainelInfo();
		
		velhaPanel = new VelhaPanel(engine, listener);

		velhaFrame.add(conexaoPanel,BorderLayout.NORTH );
		velhaFrame.add(velhaPanel  ,BorderLayout.CENTER);
		velhaFrame.add(infoPanel   ,BorderLayout.SOUTH );
		
		velhaFrame.setSize(700,550);
		velhaFrame.setLocationByPlatform(true);
		velhaFrame.setVisible(true);
	}

	/** Redesenha o tabuleiro. */
	public void redesenharTabuleiro() {
		velhaPanel.repaint();
	}

	/**
	 * Habilita ou desabilita os controles do painel de conexao.
	 * @param enabled True para habilitar, false para desabilitar
	 */
	public void habilitarPainelConexao(boolean enabled) {
	
		hostName.setEnabled(enabled);
		tcpPort.setEnabled(enabled);
		connectButton.setEnabled(enabled);
		nivelRobot.setEnabled(enabled);
	}
	
	/**
	 * Escreve um texto no painel de informacao.
	 * @param info
	 */
	public void escreverInfo(String info) {
		infoLabel.setText(info);
	}

	/**
	 * Habilita ou desabilita o botao abortar.
	 * @param enabled True para habilitar, false para desabilitar
	 */
	public void habilitarBotaoAbortar(boolean enabled) {
		abortButton.setEnabled(enabled);
	}

	/* action disparada quando algum botao e' clicado */
	@Override
	public void actionPerformed(ActionEvent event) {

		if (event.getSource() == connectButton) {
			
			velhaEngine.setNivel((Integer)nivelRobot.getValue());
			velhaListener.onClickConnect(
					hostName.getText(), (Integer)tcpPort.getValue());
			
		} else if (event.getSource() == abortButton) {

			velhaListener.onClickAbort();
		}
	}

	/** Configura o "look and feel" da aplicacao. */
	private void setLookAndFeel() {
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if (LOOK_AND_FEEL_NAME.equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** Cria o painel de conexao. */
	private void criarPainelConexao() {

		conexaoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10,10));
		conexaoPanel.setBorder(new EtchedBorder());
		
		conexaoPanel.add(new JLabel("Servidor:"));
		hostName = new JTextField(HOST_NAME_DEFAULT,10);
		conexaoPanel.add(hostName);
		
		conexaoPanel.add(new JLabel("Porta:"));
		tcpPort = new JSpinner(
				new SpinnerNumberModel(TCP_PORT_DEFAULT,1024,65535,1));
		conexaoPanel.add(tcpPort);
		
		conexaoPanel.add(new JLabel("Nivel:"));
		nivelRobot = new JSpinner(
				new SpinnerNumberModel(10,0,10,1));
		conexaoPanel.add(nivelRobot);
		
		connectButton = new JButton("Conectar");
		connectButton.addActionListener(this);
		conexaoPanel.add(connectButton);

		abortButton = new JButton("Abortar");
		abortButton.addActionListener(this);
		abortButton.setEnabled(false);
		conexaoPanel.add(abortButton);
	}
	
	/** Cria o painel de informacao. */
	private void criarPainelInfo() {

		infoPanel = new JPanel(new FlowLayout());
		infoPanel.setBorder(new EtchedBorder());
		infoLabel = new JLabel("Desconectado.");
		infoLabel.setFont(new Font(null, Font.BOLD, 14));
		infoPanel.add(infoLabel);
	}
}
