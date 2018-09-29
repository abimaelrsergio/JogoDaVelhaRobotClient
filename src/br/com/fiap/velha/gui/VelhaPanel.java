package br.com.fiap.velha.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import br.com.fiap.velha.VelhaEngine;

/**
 * Classe que implementa um painel Swing com o tabuleiro do Jogo da Velha. 
 */
@SuppressWarnings("serial")
public class VelhaPanel extends JPanel {

	/* Largura do tabuleiro, em pixels */
	private static final int WIDTH  = 400;
	/* Altura do tabuleiro, em pixels */
	private static final int HEIGHT = WIDTH;

	/* Largura de uma posicao do tabuleiro */
	private static final int X_3 = WIDTH  / 3;
	/* Altura de uma posicao do tabuleiro */
	private static final int Y_3 = HEIGHT / 3;
	
	/* Largura da linha que desenha o tabuleiro, em pixels */
	private static final float TABULEIRO_LINE_WIDTH = 4.0f;
	/* Cor do tabuleiro */
	private static final Paint TABULEIRO_COLOR      = Color.BLACK;
	
	/* Largura da linha que risca o trio ganhador, em pixels */
	private static final float FECHA_TRIO_LINE_WIDTH = 10.0f;
	/* Cor da linha que risca o trio ganhador */
	private static final Paint FECHA_TRIO_COLOR      = Color.GRAY;
	
	/* Largura da  linha que desenha o X ou O de uma jogada */
	private static final float JOGADOR_LINE_WIDTH = 8.0f;
	/* Cor do jogador X */
	private static final Paint JOGADOR_X_COLOR    = Color.RED;
	/* Cor do jogador O */
	private static final Paint JOGADOR_O_COLOR    = Color.BLUE;
	/* Espacamento entre a borda da posicao do tabuleiro e o X ou O */
	private static final int   JOGADOR_SPACING    = WIDTH / 25;

	/* Coordenada X do inicio do tabuleiro */
	private int X_START  = 0;
	/* Coordenada Y do inicio do tabuleiro */
	private int Y_START  = 0;
	/* Coordenada X do fim do tabuleiro */
	private int X_END = X_START + WIDTH;  
	/* Coordenada Y do fim do tabuleiro */
	private int Y_END = Y_START + HEIGHT;
	
	/** Armazena um conjunto de coordenadas que representam uma posicao no
	 *  tabuleiro.
	 *  coordPosicao[posicao][start|end]
	 */
	private Point coordPosicao[][] = new Point[9][2];

	/** Armazena uma referencia para um objeto VelhaEngine. */
	private VelhaEngine   velhaEngine   = null;
	/** Armazena uma referencia para um objeto VelhaListener. */
	private VelhaListener velhaListener = null;
	
	/**
	 * Construtor.
	 * @param engine Referencia para objeto VelhaEngine
	 * @param listener Referencia para objeto VelhaListener
	 */
	public VelhaPanel(VelhaEngine engine, VelhaListener listener) {
		
		this.velhaEngine   = engine;
		this.velhaListener = listener;

		calcularCoordenadas();

		addMouseListener(new MouseAdapter() {

			public void mousePressed (MouseEvent e) {
				int posicao = getPosicao(e.getX(),e.getY());
				if (posicao >= 0) {
					velhaListener.onClickTabuleiro(posicao);
					repaint();
				}
            }
			
        });
    }
	
	/* reimplementado de JPanel */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		
		calcularCoordenadas();

		desenharTabuleiro(g2d);
		desenharJogadas(g2d);
		desenharFechaTrio(g2d);
	}
	
	/**
	 * Calcula as coordenadas do tabuleiro, baseado nas dimensoes do painel
	 */
	private void calcularCoordenadas() {
		X_START  = (getSize().width  - WIDTH ) / 2;
		Y_START  = (getSize().height - HEIGHT) / 2;
		X_END = X_START + WIDTH;  
		Y_END = Y_START + HEIGHT;

		coordPosicao[0][0] = new Point(X_START      , Y_START      );
		coordPosicao[0][1] = new Point(X_START + X_3, Y_START + Y_3);

		coordPosicao[1][0] = new Point(X_START + X_3, Y_START      );
		coordPosicao[1][1] = new Point(X_END   - X_3, Y_START + Y_3);
		
		coordPosicao[2][0] = new Point(X_END   - X_3, Y_START      );
		coordPosicao[2][1] = new Point(X_END        , Y_START + Y_3);
		
		coordPosicao[3][0] = new Point(X_START      , Y_START + Y_3);
		coordPosicao[3][1] = new Point(X_START + X_3, Y_END   - Y_3);

		coordPosicao[4][0] = new Point(X_START + X_3, Y_START + Y_3);
		coordPosicao[4][1] = new Point(X_END   - X_3, Y_END   - Y_3);
		
		coordPosicao[5][0] = new Point(X_END   - X_3, Y_START + Y_3);
		coordPosicao[5][1] = new Point(X_END        , Y_END   - Y_3);

		coordPosicao[6][0] = new Point(X_START      , Y_END   - Y_3);
		coordPosicao[6][1] = new Point(X_START + X_3, Y_END        );

		coordPosicao[7][0] = new Point(X_START + X_3, Y_END   - Y_3);
		coordPosicao[7][1] = new Point(X_END   - X_3, Y_END        );
		
		coordPosicao[8][0] = new Point(X_END   - X_3, Y_END   - Y_3);
		coordPosicao[8][1] = new Point(X_END        , Y_END        );
	}

	/**
	 * Retorna uma posicao no tabuleiro, a partir das coordenadas de tela (x,y).
	 * @param x Coordenada x do painel
	 * @param y Coordenada y do painel
	 * @return Posicao no tabuleiro (0 a 8), ou -1 se coordenadas estao fora do
	 *   tabuleiro.
	 */
	private int getPosicao(int x, int y) {

		for (int pos = 0; pos < coordPosicao.length; pos++) {

			Point pStart = coordPosicao[pos][0]; 
  			Point pEnd   = coordPosicao[pos][1];
  			
			if (x > pStart.x && x < pEnd.x &&
				y > pStart.y && y < pEnd.y) {
				return pos;
			}
		}
		return -1;
	}

	/**
	 * Desenha o tabuleiro no painel.
	 * @param g2d Referencia para objeto Graphics2D, da API Java2D
	 */
	private void desenharTabuleiro(Graphics2D g2d) {

		g2d.setStroke(new BasicStroke(TABULEIRO_LINE_WIDTH));
		g2d.setPaint(TABULEIRO_COLOR);
		
		g2d.drawLine(X_START, Y_START + Y_3, X_END, Y_START + Y_3);
		g2d.drawLine(X_START, Y_END   - Y_3, X_END, Y_END   - Y_3);
	
		g2d.drawLine(X_START + X_3, Y_START, X_START + X_3, Y_END);
		g2d.drawLine(X_END   - X_3, Y_START, X_END   - X_3, Y_END);
	}

	/**
	 * Desenha as jogadas no painel, dentro do tabuleiro.
	 * @param g2d Referencia para objeto Graphics2D, da API Java2D
	 */
	private void desenharJogadas(Graphics2D g2d) {

		g2d.setStroke(new BasicStroke(JOGADOR_LINE_WIDTH));
		
		for (int pos = 0; pos < 9; pos++) {
			
			Point pStart = coordPosicao[pos][0]; 
  			Point pEnd   = coordPosicao[pos][1];
  			
  			int x1 = pStart.x + JOGADOR_SPACING;
  			int x2 = pEnd.x   - JOGADOR_SPACING;
  			int y1 = pStart.y + JOGADOR_SPACING;
  			int y2 = pEnd.y   - JOGADOR_SPACING;

  			if (velhaEngine.getPosicao(pos) == VelhaEngine.JOGADOR_X) {

  				/* desenha um X */
  				g2d.setPaint(JOGADOR_X_COLOR);
	  			g2d.drawLine(x1, y1, x2, y2);
	  			g2d.drawLine(x1, y2, x2, y1);
	  			
			} else if (velhaEngine.getPosicao(pos) == VelhaEngine.JOGADOR_O) {

				/* desenha um O */
  				g2d.setPaint(JOGADOR_O_COLOR);
	  			g2d.drawOval(x1, y1, x2-x1, y2-y1);
				
			}
		}
	}
	
	/**
	 * Desenha uma linha sobre o trio ganhador, se houver.
	 * @param g2d Referencia para objeto Graphics2D, da API Java2D
	 */
	private void desenharFechaTrio(Graphics2D g2d) {
		
		int trio[] = velhaEngine.getTrioGanhador();
		
		if (trio == null || trio.length < 3) return;
		
		final int spacing = X_3 / 2;
		
		int x1 = coordPosicao[trio[0]][0].x + spacing;
		int y1 = coordPosicao[trio[0]][0].y + spacing;
		int x2 = coordPosicao[trio[2]][0].x + spacing;
		int y2 = coordPosicao[trio[2]][0].y + spacing;

		g2d.setStroke(new BasicStroke(FECHA_TRIO_LINE_WIDTH));
		g2d.setPaint(FECHA_TRIO_COLOR);
		
		g2d.drawLine(x1, y1, x2, y2);
	}
	
}
