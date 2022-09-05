package com.coinman.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class CoinMan extends ApplicationAdapter {
	SpriteBatch batch;
	int gameState = 0; //0: Waiting to start/game end, 1: game live
	int score = 0;

	Texture background;
	BitmapFont scoreFont;

	Texture[] man;
	Texture dizzy;
	Rectangle manRectangle;
	int manState = 0;
	int manY = 0;
	float gravity = 0.2f;
	float velocity = 0;

	Texture coin;
	int coinCount = 0;
	ArrayList<Integer> coinX = new ArrayList<>();
	ArrayList<Integer> coinY = new ArrayList<>();
	ArrayList<Rectangle> coinRectangle = new ArrayList<>();
	Random random;

	Texture bomb;
	int bombCount = 0;
	ArrayList<Integer> bombX = new ArrayList<>();
	ArrayList<Integer> bombY = new ArrayList<>();
	ArrayList<Rectangle> bombRectangle = new ArrayList<>();
	
	@Override
	public void create () {
		batch = new SpriteBatch();

		//score font
		scoreFont = new BitmapFont();
		scoreFont.setColor(Color.WHITE);
		scoreFont.getData().setScale(10);

		//background
		background = new Texture("bg.png");

		//man
		man = new Texture[]{new Texture("frame-1.png"),
							new Texture("frame-2.png"),
							new Texture("frame-3.png"),
							new Texture("frame-4.png")};
		manY = Gdx.graphics.getHeight() / 2;
		dizzy = new Texture("dizzy-1.png");

		//coin
		coin = new Texture("coin.png");
		random = new Random();

		//bomb
		bomb = new Texture("bomb.png");
	}

	//make a new bomb
	public void makeNewBomb(){
		float height = random.nextFloat() * Gdx.graphics.getHeight();
		bombY.add((int) height);
		bombX.add(Gdx.graphics.getWidth());
	}
	//drawing bomb(every 200 loop)
	public void drawBomb(){
		if(bombCount < 200){
			bombCount++;
		}
		else{
			bombCount = 0;
			makeNewBomb();
		}

		bombRectangle.clear();
		HashSet<Integer> outOfSpaceBomb = new HashSet<>();
		for(int i=0; i<bombX.size(); i++){
			batch.draw(bomb, bombX.get(i), bombY.get(i)); //draw bomb
			bombX.set(i, bombX.get(i)-4); //move bomb
			bombRectangle.add(new Rectangle(bombX.get(i), bombY.get(i), bomb.getWidth(), bomb.getHeight())); // add rectangle
			if(bombX.get(i) < -bomb.getWidth()){
				outOfSpaceBomb.add(i); //remove out of space coin
			}
		}
		for(int x: outOfSpaceBomb){
			bombX.remove(x);
			bombY.remove(x);
		}
	}

	//make a new coin
	public void makeNewCoin(){
		float height = random.nextFloat() * Gdx.graphics.getHeight();
		coinY.add((int) height);
		coinX.add(Gdx.graphics.getWidth());
	}

	//drawing coin(every 100 loop)
	public void drawCoin(){
		if(coinCount < 100){
			coinCount++;
		}
		else{
			coinCount = 0;
			makeNewCoin();
		}

		coinRectangle.clear();
		HashSet<Integer> outOfSpaceCoin = new HashSet<>();
		for(int i=0; i<coinX.size(); i++){
			batch.draw(coin, coinX.get(i), coinY.get(i)); //draw coin
			coinX.set(i, coinX.get(i)-4); //move coin
			coinRectangle.add(new Rectangle(coinX.get(i), coinY.get(i), coin.getWidth(), coin.getHeight())); // add rectangle
			if(coinX.get(i) < -coin.getWidth()){
				outOfSpaceCoin.add(i); //remove out of space coin
			}
		}
		for(int x: outOfSpaceCoin){
			coinX.remove(x);
			coinY.remove(x);
			coinRectangle.remove(x);
		}
	}

	//drawing man ( pause = 8)
	public void drawMan(){
		if(gameState == 1){
			//running
			if(manState < 31){
				manState++;
			}
			else{
				manState = 0;
			}
			//jumping
			//goes up
			if(Gdx.input.justTouched()){
				velocity = -10;
			}
			//falls down
			velocity += gravity;
			manY -= velocity;
			if(manY <= 0) {
				manY = 0;
			}
		}
		//draw the man
		Texture currentMan = (gameState == 1)? man[manState/8] : dizzy;
		batch.draw(currentMan, Gdx.graphics.getWidth()/2 - currentMan.getWidth()/2, manY);
		//build man rectangle
		manRectangle = new Rectangle(Gdx.graphics.getWidth()/2 - currentMan.getWidth()/2, manY,
									currentMan.getWidth(), currentMan.getHeight());
	}

	@Override
	public void render () {
		batch.begin();

		//Waiting for beginning/game End
		if(gameState == 0){
			if(Gdx.input.justTouched()){
				gameState = 1;
				manY = Gdx.graphics.getHeight()/2;
				score = 0;
				velocity = 0;
				coinX.clear();
				coinY.clear();
				coinRectangle.clear();
				coinCount = 0;
				bombX.clear();
				bombY.clear();;
				bombRectangle.clear();
				bombCount = 0;
			}
		}

		//drawing background
		batch.draw(background,0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		//drawing man
		drawMan();

		if(gameState == 1){
			//drawing coins
			drawCoin();
			//drawing bombs
			drawBomb();

			//collision of coin
			for(int i=0; i<coinRectangle.size(); i++){
				if(Intersector.overlaps(manRectangle, coinRectangle.get(i))){
					score++;
					coinX.remove(i);
					coinY.remove(i);
					coinRectangle.remove(i);
					break;
				}
			}
			scoreFont.draw(batch, String.valueOf(score), 100, 200);

			//collsion of bomb
			for(int i=0; i<bombRectangle.size(); i++){
				if(Intersector.overlaps(manRectangle, bombRectangle.get(i))){
					gameState = 0;
					break;
				}
			}
		}

		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}
}
