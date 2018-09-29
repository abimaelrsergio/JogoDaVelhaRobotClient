package br.com.fiap.velha.client;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import br.com.fiap.velha.gui.VelhaListener;

/**
 * Classe que implementa o manipulador de sockets do Cliente do Jogo da Velha. 
 */
public class VelhaHandler extends IoHandlerAdapter {

	/** Indica se o Modo Debug esta' ativado. */
	private static final boolean DEBUG_ENABLED = false;

	/** Armazena uma referencia para o objeto VelhaListener. */
	private VelhaListener listener = null;
	
	/** Guarda a sessao corrente (conexao com o servidor). */
	private IoSession session = null;
	
	/**
	 * Construtor.
	 * @param listener Referencia para o objeto VelhaListener que recebera' os
	 *   eventos de rede
	 */
	public VelhaHandler(VelhaListener listener) {

		this.listener = listener;
	}
	
	/**
	 * Evento disparado quando uma sessao (conexao) e' aberta.
	 * @param session Referencia para objeto que representa a sessao
	 */
	@Override
	public void sessionOpened(IoSession session) {
		
		this.session = session;
		listener.onSessionOpened();
	 }

	/**
	 * Evento disparado quando uma sessao (conexao) e' fechada.
	 * @param session Referencia para objeto que representa a sessao
	 */
	 @Override
	 public void sessionClosed(IoSession session) {

		 this.session = null;
		 listener.onSessionClosed();
	 }

	/**
	 * Evento disparado quando uma mensagem e' recebida.
	 * @param session Referencia para objeto que representa a sessao
	 * @param message Mensagem recebida
	 */
	 @Override
	 public void messageReceived(IoSession session, Object message) {

		 if (DEBUG_ENABLED) System.out.println("Recebida:" + (String) message);
		 listener.onMessageReceived((String) message);
	 }

	/**
	 * Envia uma mensagem para o servidor. 
	 * @param message Mensagem a ser enviada
	 */
	public void sendMessage(String message) {

		if (session != null && session.isConnected()) {
			session.write(message);
		}
		
	}

	/**
	 * Em caso de excecao, este metodo e' disparado.
	 * @param session Referencia para objeto que representa a sessao
	 * @param cause Excecao ocorrida
	 */
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) {

		if (DEBUG_ENABLED) cause.printStackTrace();
		session.close(true);
	}
	
}
