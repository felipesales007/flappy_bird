package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import java.util.Random;

public class MyGdxGame extends ApplicationAdapter
{
	private SpriteBatch batch;// CRIA AS IMAGENS SEQUENCIAS PARA MOVIMENTOS
	private Texture[] passaros; // VETOR DE IMAGENS PARA SER PASSADA EM MOVIMENTO
	private Texture fundo; // IMAGEM DE FUNDO
	private Texture canoTopo; // IMAGEM DO CANO QUE FICA NO TOPO
	private Texture canoBaixo;	// IMAGEM DO CANO QUE FICA EM BAIXO
	private Texture gameOver; // IMAGEM DE GAME OVER
	private Random numeroRandomico; // PARA GERAR NUMERO RANDOMICO
	private BitmapFont fonte; // PARA FONTE DE PONTUAÇÃO
	private BitmapFont melhorPonto; // PARA FONTE DA MELHOR PONTUAÇÃO
	private Rectangle passaroRetangulo; // RETANGULO DO PASSARO PARA INSERIR A BATIDA
	private Rectangle retanguloCanoTopo; // RETANGULO DO CANO PARA INSERIR A BATIDA
	private Rectangle retanguloCanoBaixo; // RETANGULO DO CANO PARA INSERIR A BATIDA
	private ShapeRenderer shape; // PARA DESENHAR OS OBJETOS PARA A BATIDA
	private Sound ponto; // PARA A SOM DE PONTUAÇÃO
	private Music musicaTema; // PARA MUSICA TEMA DO JOGO

	private int estadoJogo = 0; // 0 -> JOGO NÃO INICIADO, 1 -> JOGO INICIADO, 2 -> JOGO GAME OVER
	private int pontuacao = 0; // CONTADOR DA PONTUAÇÃO
	private int melhorPontuacao = 0; // CONTADOR DA MELHOR PONTUAÇÃO
	private int larguraDispositivo; // PARA DEFINIR A LARGURA DO DISPOSITIVO PARA NÃO TER QUE FICAR REPETINDO TODA HORA
	private int alturaDispositivo; // PARA DEFINIR A ALTURA DO DISPOSITIVO PARA NÃO TER QUE FICAR REPETINDO TODA HORA

	private float posicaoInicial; // PARA DEFICIR A POSIÇÃO INICIAL DO PASSARO NA TELA
	private float posicaoCano; // PARA A IMAGEM DO CANO QUE COMEÇA NO LADO DIREITO DA TELA
	private float espacoEntreCanos; // PARA A IMAGEM DO CANO QUE VAI DEIXA UM ESPAÇO ENTRE OS DOIS CANOS
	private float alturaEntreCanosRandomica; // PARA O CANO SE MOVER ALEATORIAMENTE
	private float variacao = 0; // PARA VARIAR O "VETOR" CRIANDO AS IMAGENS SEQUENCIAS PARA OS MOVIMENTOS
	private float queda = 0; // PARA A QUEDA DO PASSARO
	private float deltaTime; // PARA DEFINIR A FUNÇÃO DE TEMPO GDX (MOVIMENTAÇÃO)

	private boolean marcouPonto = false; // PONTUAÇÃO INICIA COMO FALSA (DESATIVADA / ZERADA)

	@Override
	public void create ()
	{
		try // CASO DÊ ALGUM ERRO
		{
			FileHandle fileo = Gdx.files.local("melhorPonto.txt"); // LÊ O ARQUIVO COM A MELHOR PONTUAÇÃO JÁ SALVA
			melhorPontuacao = Integer.parseInt(fileo.readString()); // MELHOR PONTUAÇÃO RECEBE A MELHOR PONTUAÇÃO JÁ SALVA
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		batch = new SpriteBatch(); // PARA OS MOVIMENTOS QUE SERÁ CRIADA COM PASSAROS[]
		numeroRandomico = new Random(); // PARA O MODO RANDOM (NUMEROS ALEATORIOS)
		passaroRetangulo = new Rectangle(); // RETANGULO DO PASSARO PARA A BATIDA
		retanguloCanoTopo = new Rectangle(); // RETANGULO DO CANO TOPO PARA A BATIDA
		retanguloCanoBaixo = new Rectangle(); // RETANGULO DO CANO BAIXO PARA A BATIDA
		shape = new ShapeRenderer(); // PARA DESENHAR OS OBJETOS RETANGULO E CIRCULO PARA A BATIDA
		passaros = new Texture[3]; // DEFINIÇÃO QUE SERÁ SOMENTE 3 IMAGENS PASSADAS COMO MOVIMENTO
		passaros[0] = new Texture("imagem_passaro1.png");
		passaros[1] = new Texture("imagem_passaro2.png");
		passaros[2] = new Texture("imagem_passaro3.png");
		fundo = new Texture("imagem_fundo.png"); // IMAGEM DO FUNDO
		canoTopo = new Texture("imagem_cano_topo.png"); // IMAGEM DO CANO DO TOPO
		canoBaixo = new Texture("imagem_cano_baixo.png"); // IMAGEM DO CANO DE BAIXO
		gameOver = new Texture("imagem_game_over.png"); // IMAGEM DE GAME OVER
		ponto = Gdx.audio.newSound(Gdx.files.internal("ponto.wav")); // SOM DO JOGO
		musicaTema = Gdx.audio.newMusic(Gdx.files.internal("musicaTema.mp3")); // MÚSIA DE FUNDO
		musicaTema.setLooping(true); // HABILITA PARA O LOOP NO FINAL DA MUSICA

		fonte = new BitmapFont(); // FONTE
		fonte.setColor(Color.WHITE); // COR DA FONTE
		fonte.getData().setScale(9); // TAMANHO DA FONTE
		melhorPonto = new BitmapFont(); // FONTE
		melhorPonto.setColor(Color.GOLD); // COR DA FONTE
		melhorPonto.getData().setScale(7); // TAMANHO DA FONTE

		larguraDispositivo = Gdx.graphics.getWidth(); // DEFINIÇÃO DE LARGURA COM GDX
		alturaDispositivo = Gdx.graphics.getHeight(); // DEFINIÇÃO DE ALTURA COM GDX
		posicaoInicial = alturaDispositivo / 2; // DEFINIÇÃO DO PONTO DE INICIO DO PASSARO NA TELA
		posicaoCano = larguraDispositivo; // DEFINIÇÃO DO PONTO DE INICIO DO CANO NA TELA NO LADO DIREITO
		espacoEntreCanos = alturaDispositivo - 700; // DEFINIÇÃO DO ESPEÇO ENTRE OS CANOS
	}

	@Override
	public void render ()
	{
		musicaTema.play(); // MÚSICA DE FUNDO
		deltaTime = Gdx.graphics.getDeltaTime(); // FAZ COM QUE O CONTADOR SEJA MAIS SUAVE (MELHOR QUE UTILIZAR ++ OU --)
		variacao += deltaTime * 10; // FAZ COM QUE O MOVIMENTO DA IMAGEM FIQUE LENTA E SUAVE COM 10X MAIS DE VELOCIDADE
		if(variacao > 3) variacao = 0; // DEFINIÇÃO DE QUE QUANDO A IMAGEM FOR A ULTIMA ELA VOLTA A REPETIR (LOOP DOS MOVIMENTOS)

		if(estadoJogo == 0) // JOGO NÃO INICIADO
		{
			if (Gdx.input.justTouched()) // VERIFICA SE TOCOU NA TELA
			{
				estadoJogo = 1; // TOCOU NA TELA JOGO INICIA
			}
		}
		if(estadoJogo == 1) // JOGO INICIADO
		{
			posicaoCano -= deltaTime * 400; // MOVIMENTA O CANO
			queda ++; // QUEDA DO PASSARO

			if(Gdx.input.justTouched()) // SE A TELA FOR TOCADA O PASSARO SOBE
			{
				queda = - deltaTime * 1200;
			}

			// VERIFICA SE O CANO SAIU INTEIRAMENTE DA TELA
			if(posicaoCano < - larguraDispositivo / 4)
			{
				posicaoCano = larguraDispositivo; // REINICIA A POSIÇÃO DO CANO PARA O LADO DIREITO QUANDO CHEGA AO FINAL DO LADO ESQUERDO
				alturaEntreCanosRandomica = numeroRandomico.nextInt(700) - 500; // GERAÇÃO DE ALTURA DE CANOS ALEATORIOS
				marcouPonto = false; // ENQUANTO O PASSARO NÃO PASSAR DO CANO A PONTUAÇÃO FICA DESABILITADA
			}

			// VERIICA PONTUAÇÃO
			if(posicaoCano < posicaoInicial / 15) // QUANDO O CANO PASSAR DO PASSARO
			{
				if(!marcouPonto) // PONTUAÇÃO NÃO MARCADA
				{
					ponto.play(0.7f); // INSERÇÃO DO SOM, VOLUME VARIA ENTRE 0 A 1
					pontuacao++; // PONTUAÇÃO MARCADA COM INCREMENTO DE  +1
					marcouPonto = true; // PONTUAÇÃO HABILITADA PARA MARCAÇÃO DE PONTO
				}
			}

			if(posicaoInicial > 0 || queda < 0) // IMPEDE QUE O PASSARO ULTRAPASSE A PARTE DE BAIXO DA TELA NA QUEDA
			{
				posicaoInicial = posicaoInicial - queda; // FAZ COM QUE O PASSARO CONTINUI CAINDO
			}
		}
		if(estadoJogo == 2) // TELA DE GAME OVER
		{
			queda ++; // QUEDA DO PASSARO

			if(pontuacao > melhorPontuacao)
			{
				melhorPontuacao = pontuacao; // SALVA A MELHOR PONTUAÇÃO DO JOGO

				FileHandle file = Gdx.files.local("melhorPonto.txt"); // SALVA EM ARQUIVO
				file.writeString(String.valueOf(melhorPontuacao), false);
			}

			if(Gdx.input.justTouched()) // RESETA OS ESTADOS E ATRIBUIÇÕES
			{
				estadoJogo = 0;
				pontuacao = 0;
				queda = 0;
				posicaoInicial = alturaDispositivo / 2;
				posicaoCano = larguraDispositivo;
			}

			if(posicaoInicial > 0 || queda < 0) // IMPEDE QUE O PASSARO ULTRAPASSE A PARTE DE BAIXO DA TELA NA QUEDA
			{
				posicaoInicial = posicaoInicial - queda; // FAZ COM QUE O PASSARO CONTINUI CAINDO
			}
		}

		batch.begin(); // PARA INICIALIZAÇÃO DOS MOVIMENTOS

		batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo); // INSERÇÃO DO FUNDO DO JOGO COM AS DEFINIÇÕES DE ALTURA E LARGURA JA DECLARADAS
		batch.draw(canoTopo, posicaoCano, alturaDispositivo / 2 + espacoEntreCanos / 2 + alturaEntreCanosRandomica, larguraDispositivo / 6, alturaDispositivo); // INSERÇÃO DO CANO DO TOPO
		batch.draw(canoBaixo, posicaoCano, alturaDispositivo / 3 - canoBaixo.getHeight() - espacoEntreCanos / 2 + alturaEntreCanosRandomica, larguraDispositivo / 6, alturaDispositivo); // INSERÇÃO DO CANO DO BAIXO
		batch.draw(passaros[(int) variacao], larguraDispositivo / 4, posicaoInicial, alturaDispositivo / 15, larguraDispositivo / 15); // INSERÇÃO DO PASSARO COM O MOVIMENTO E LOCALIDADE NA TELA
		fonte.draw(batch, String.valueOf(pontuacao), larguraDispositivo / 2.2f, alturaDispositivo / 1.1f); // INSERÇÃO DO PONTO NA TELA DO JOGO NO MEIO DO TOPO DA TELA

		if (estadoJogo == 2) // ESTADO DE GAME OVER
		{
			batch.draw(gameOver, larguraDispositivo / 5.75f, alturaDispositivo / 1.5f, larguraDispositivo / 1.5f, alturaDispositivo / 8); // INSERÇÃO DA IMAGEM DE GAME OVER
			melhorPonto.draw(batch, String.valueOf(melhorPontuacao), larguraDispositivo / 1.2f, alturaDispositivo / 1.02f);  // INSERÇÃO DO TEXTO DE MELHOR PONTUAÇÃO
		}

		batch.end(); // PARA FIM DOS MOVIMENTOS


		// DECLARAÇÕES DE FORMATOS DOS OBJETOS QUE IMPLEMENTARAM A BATIDA
		passaroRetangulo.set(larguraDispositivo / 4, posicaoInicial, alturaDispositivo / 15, larguraDispositivo / 15); // DEFINIÇÃO DO TAMANHO DO CIRCULO DO PASSARO
		retanguloCanoBaixo.set(posicaoCano, alturaDispositivo / 3 - canoBaixo.getHeight() - espacoEntreCanos / 2 + alturaEntreCanosRandomica, larguraDispositivo / 6, alturaDispositivo); // DEFINIÇÃO O TAMANHO DO CIRCULO DO PASSARO
		retanguloCanoTopo.set(posicaoCano, alturaDispositivo / 2 + espacoEntreCanos / 2 + alturaEntreCanosRandomica, larguraDispositivo / 6, alturaDispositivo); // DEFINIÇÃO DO TAMANHO DO CIRCULO DO PASSARO

		//DESENHA FORMAS NA TELA PARA COLISÕES, CODIGOS DESNECESSARIOS, CRIADOS APENAS PARA ILUSTRAÇÃO E VERIFICAÇÃO DOS OBJETOS NA BATIDA
		/*shape.begin(ShapeRenderer.ShapeType.Filled); // PARA PREENCHAR AS FORMAS

		shape.rect(passaroRetangulo.x, passaroRetangulo.y, passaroRetangulo.width, passaroRetangulo.height); // DEFINIÇÃO DO TAMANHO DO RETANGULO DO PASSARO (UTILIZANDO AS REFERENCIAS ACIMA)
		shape.rect(retanguloCanoBaixo.x, retanguloCanoBaixo.y,retanguloCanoBaixo.width, retanguloCanoBaixo.height); // DEFINIÇÃO DO TAMANHO DO RETANGULO DO PASSARO (UTILIZANDO AS REFERENCIAS ACIMA)
		shape.rect(retanguloCanoTopo.x, retanguloCanoTopo.y,retanguloCanoTopo.width, retanguloCanoTopo.height); // DEFINIÇÃO DO TAMANHO DO RETANGULO DO PASSARO (UTILIZANDO AS REFERENCIAS ACIMA)
		shape.setColor(Color.RED); // DEFINIÇÃO DE COR PARA AS FORMAS DOS OBJETOS QUE IMPLEMENTARAM A BATIDA

		shape.end();*/

		// VERIFICAÇÃO DE COLISÕES COM OS OBJETOS CRIADOS OU BATER NO TETO OU NO CHÃO DO JOGO
		if(Intersector.overlaps(passaroRetangulo, retanguloCanoBaixo) || Intersector.overlaps(passaroRetangulo, retanguloCanoTopo) || posicaoInicial <= 0 || posicaoInicial >= alturaDispositivo )
		{
			estadoJogo = 2; // ESTADO DO JOGO IGUAL A 2 PARA GAME OVER
		}
	}
}
