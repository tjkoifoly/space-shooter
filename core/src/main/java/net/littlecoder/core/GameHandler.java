package net.littlecoder.core;

import static playn.core.PlayN.*;

import java.util.ArrayDeque;
import java.util.Iterator;

import playn.core.CanvasImage;
import playn.core.Font;
import playn.core.Key;
import playn.core.Keyboard;
import playn.core.Surface;
import playn.core.TextFormat;
import playn.core.TextLayout;

class GameHandler implements Keyboard.Listener {

    private static final float SCORE_FONT_SIZE = 15f;

    private Surface surface;

    private Ship ship;
    private ArrayDeque<Asteroid> asteroids = new ArrayDeque<Asteroid>();
    private ArrayDeque<Bullet> bullets = new ArrayDeque<Bullet>();
    private boolean shooting = false;

    private byte lifes;
    private int score;
    private int level;

    private TextFormat smallTextFormat;
    private CanvasImage scoreImage;
    private CanvasImage levelImage;

    private Polyline shipPolyline;

    public GameHandler(Surface surface) {
	this.surface = surface;

	initTexts();
	initLevel(1);

	shipPolyline = Ship.shipPolyline.clone();

	keyboard().setListener(this);
    }

    public void paint(float alpha) {
	surface.clear();
	
	if (ship != null)
	    ship.paint(alpha);

	Iterator i = bullets.iterator();
	while (i.hasNext()) {
	    Bullet b = (Bullet)i.next();
	    b.paint(alpha);
	}

	i = asteroids.iterator();
	while (i.hasNext()) {
	    Asteroid a = (Asteroid)i.next();
	    a.paint(alpha);
	}

	for (int l = 0; l < lifes; l++)
	    shipPolyline.transform(0f, 20f + l * 20f, 20f).paint(surface);
	
	paintScore();
	paintLevel();
    }

    public void update(float delta) {
	updateShip(delta);
	updateBullets(delta);
	updateAsteroids(delta);
	detectCollisions(delta);
    }

    public void onKeyDown(Keyboard.Event event) {
	if (ship != null) {
	    if (event.key() == Key.UP)
		ship.accelerate(true);
	    if (event.key() == Key.RIGHT)
		ship.steerRight(true);
	    if (event.key() == Key.LEFT)
		ship.steerLeft(true);
	    if (!ship.isDisabled() && event.key() == Key.SPACE)
		shooting = true;
	} else {
	    if (event.key() == Key.SPACE)
		ship = new Ship(surface);
	}

	if (event.key() == Key.ESCAPE)
	    System.exit(0);
    }

    public void onKeyUp(Keyboard.Event event) {
	if (ship != null) {
	    if (event.key() == Key.UP)
		ship.accelerate(false);
	    if (event.key() == Key.RIGHT)
		ship.steerRight(false);
	    if (event.key() == Key.LEFT)
		ship.steerLeft(false);
	}
    }
    
    public void onKeyTyped(Keyboard.TypedEvent event) {}

    private void updateBullets(float delta) {
	Iterator i = bullets.iterator();
	while (i.hasNext()) {
	    Bullet b = (Bullet)i.next();
	    b.update(delta);
	    if (b.isDead())
		i.remove();
	}

 	if (shooting) {
	    bullets.add(new Bullet(ship));
	    shooting = false;
	}
   }

    private void updateShip(float delta) {
	if (ship != null) {
	    ship.update(delta);
	    if (ship.isDead()) {
		lifes --;
		ship = null;
	    }
	}
    }

    private void updateAsteroids(float delta) {
	ArrayDeque<Asteroid> newAsteroids = new ArrayDeque<Asteroid>();

	Iterator i = asteroids.iterator();
	while (i.hasNext()) {
	    Asteroid a = (Asteroid)i.next();
	    if (a.isDead()) {
		Asteroid[] children = a.spawnChildren();
		for (Asteroid c : children)
		    newAsteroids.add(c);
		i.remove();
	    } else
		a.update(delta);
	}

	i = newAsteroids.iterator();
	while (i.hasNext())
	    asteroids.add((Asteroid)i.next());
    }

    private void detectCollisions(float delta) {
	Iterator i = asteroids.iterator();
	while (i.hasNext()) {
	    Asteroid a = (Asteroid)i.next();
	    Iterator j = bullets.iterator();
	    while (j.hasNext()) {
		Bullet b = (Bullet)j.next();
		if (!b.isDead() && a.isCollidingWith(b)) {
		    a.die();
		    b.die();
		    score += 3 - a.size();
		}
	    }
	}

	if (ship != null) {
	    i = asteroids.iterator();
	    while (i.hasNext()) {
		Asteroid a = (Asteroid)i.next();
		if (a.isCollidingWith(ship))
		    ship.die();
	    }
	}
    }

    private void paintScore() {
	String scoreText = String.valueOf(score);
	if (score < 10)
	    scoreText = "0" + scoreText;
	if (score < 100)
	    scoreText = "0" + scoreText;
	if (score < 1000)
	    scoreText = "0" + scoreText;
	if (score < 10000)
	    scoreText = "0" + scoreText;

	TextLayout layout = graphics().layoutText(scoreText, smallTextFormat);

	scoreImage.canvas().clear();
	scoreImage.canvas().drawText(layout, 0, 0);
	surface.drawImage(scoreImage, surface.width() - layout.width() - 10, 10f);
    }

    private void paintLevel() {
	String levelText = String.valueOf(level);
	if (level < 10)
	    levelText = "0" + levelText;

	TextLayout layout = graphics().layoutText(levelText, smallTextFormat);

	levelImage.canvas().clear();
	levelImage.canvas().drawText(layout, 0, 0);
	surface.drawImage(levelImage, surface.width() / 2f - layout.width(), 10f);
    }

    private void initTexts() {
	Font font = graphics().createFont("Vector Battle", Font.Style.PLAIN, SCORE_FONT_SIZE);
        smallTextFormat = new TextFormat().withFont(font).withTextColor(0xFFFFFFFF);

	TextLayout l = graphics().layoutText("00", smallTextFormat);
	levelImage = graphics().createImage(
	    (int)Math.ceil(l.width()),
	    (int)Math.ceil(l.height())
        );

	l = graphics().layoutText("00000", smallTextFormat);
	scoreImage = graphics().createImage(
	    (int)Math.ceil(l.width()),
	    (int)Math.ceil(l.height())
        );
    }

    private void initLevel(int level) {
	this.level = level;
	if (level == 1) {
	    lifes = 3;
	    score = 0;
	    ship = new Ship(surface);
	}

	asteroids.clear();
	for (int i = 0; i < level + 2; i++)
	    asteroids.add(new Asteroid((byte)2, surface));
    }

}