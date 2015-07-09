package meshtool.frame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import meshtool.frame.OperateMode.PointEditModeType;
import meshtool.frame.OperateMode.RunMode;

/**
 *
 * @author Administrator
 */
public class GLCavans extends ApplicationAdapter {

    Stage stage;
    Stage editStage;
    BitmapFont font;
    Label fpsLable;
    Viewport viewport;
    OrthographicCamera camera;
    Texture tex;
    Image image;
    String imagePath;
    ShapeRenderer renderer;

    PolygonSpriteBatch polygonBatch;
    PolygonSprite polygonSprite;

    MainFrame mf;
    TimeFrame tf;
    int maxFrameIndex = 0;
    boolean isPlayingAnimation;
    boolean isLoop = true;

    public PointEditModeType currentModeType;
    public List<Vector2> points = new ArrayList<>();
    public List<Vector2> pointsAnimation = new ArrayList<>();
    public List<Vector2> selectedPoints = new ArrayList<>();
    public List<Vector2> selectedPointsAnimation = new ArrayList<>();
    public List<Vector2[]> triangleList = new ArrayList<>();
    public Vector2 pointPoint;

    public RunMode currentRunMode = RunMode.VertexEdit;

    Rectangle selectRect;

    float zoom;

    float[] vertices;
    TimeFrame.Frame[] frames;
    short[] triangles;

    public void changeToAnimationMode() {
        if (image == null) {
            return;
        }
        isPlayingAnimation=false;
        currentRunMode = RunMode.AnimateEdit;
        currentModeType = PointEditModeType.Normal;

        vertices = new float[points.size() * 2];
        triangles = new short[triangleList.size() * 3];
        frames = new TimeFrame.Frame[vertices.length];

        pointsAnimation.clear();
        selectedPointsAnimation.clear();

        tf.frames.clear();

        int i = 0;
        for (Vector2 vec : points) {
            frames[i] = tf.new Frame("", vec.x);
            frames[i].addFrame(0, vec.x);
            tf.frames.add(frames[i]);
            vertices[i++] = vec.x;
            frames[i] = tf.new Frame("", vec.y);
            frames[i].addFrame(0, vec.y);
            tf.frames.add(frames[i]);
            vertices[i++] = vec.y;
            pointsAnimation.add(new Vector2(vec.x, vec.y));
        }
        i = 0;
        for (Vector2[] vecs : triangleList) {
            triangles[i++] = (short) points.indexOf(vecs[0]);
            triangles[i++] = (short) points.indexOf(vecs[1]);
            triangles[i++] = (short) points.indexOf(vecs[2]);
        }

        tf.clearFrameChangeEvent();
        tf.addFrameChangeEvent(new TimeFrame.FrameChangeEvent() {

            @Override
            public void changeEvent(int currentFrame) {
                for (int i = 0; i < frames.length; i++) {
                    int pV = i / 2;
                    vertices[pV * 5 + (i % 2)] = frames[i].getFrameValue(currentFrame);
                    Vector2 v = pointsAnimation.get(pV);
                    if (i % 2 == 0) {
                        v.x = frames[i].getFrameValue(currentFrame);
                    } else {
                        v.y = frames[i].getFrameValue(currentFrame);
                    }
                }
            }
        });

        tf.repaint();

        polygonSprite = new PolygonSprite(new PolygonRegion(new TextureRegion(tex), vertices, triangles));
        vertices = polygonSprite.getVertices();

        System.out.println(Arrays.toString(vertices));
        System.out.println(Arrays.toString(polygonSprite.getRegion().getTriangles()));
    }

    public void changeToVertexEditMode() {
        currentRunMode = RunMode.VertexEdit;
    }

    public void loadImage(String path) {
        this.imagePath = path;
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (tex != null) {
                    tex.dispose();
                    tex = null;
                    image.remove();
                    image = null;
                }
                tex = new Texture(Gdx.files.absolute(imagePath));
                tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                image = new Image(tex);
                editStage.addActor(image);
                image.setPosition(0, 0);
            }
        });
    }

    public void addPointsToTrangle() {
        if (currentRunMode == RunMode.AnimateEdit) {
            return;
        }

        if (selectedPoints.size() != 3) {
            return;
        }
        Vector2[] vs = new Vector2[]{selectedPoints.get(0), selectedPoints.get(1), selectedPoints.get(2)};
        for (Vector2[] ovs : triangleList) {
            if (ovs[0] == vs[0] && ovs[1] == vs[1] && ovs[2] == vs[2]
                    || ovs[0] == vs[0] && ovs[1] == vs[2] && ovs[2] == vs[1]
                    || ovs[0] == vs[1] && ovs[1] == vs[2] && ovs[2] == vs[0]
                    || ovs[0] == vs[1] && ovs[1] == vs[0] && ovs[2] == vs[2]
                    || ovs[0] == vs[2] && ovs[1] == vs[0] && ovs[2] == vs[1]
                    || ovs[0] == vs[2] && ovs[1] == vs[1] && ovs[2] == vs[0]) {
                return;
            }
        }
        triangleList.add(vs);

        mf.updateTriangleList(triangleList.toArray());

        selectedPoints.clear();
    }

    public void deleteTrangle() {
        if (currentRunMode == RunMode.AnimateEdit) {
            return;
        }
        if (selectedPoints.size() != 3) {
            return;
        }
        Vector2[] vs = new Vector2[]{selectedPoints.get(0), selectedPoints.get(1), selectedPoints.get(2)};
        for (Vector2[] ovs : triangleList) {
            if (ovs[0] == vs[0] && ovs[1] == vs[1] && ovs[2] == vs[2]
                    || ovs[0] == vs[0] && ovs[1] == vs[2] && ovs[2] == vs[1]
                    || ovs[0] == vs[1] && ovs[1] == vs[2] && ovs[2] == vs[0]
                    || ovs[0] == vs[1] && ovs[1] == vs[0] && ovs[2] == vs[2]
                    || ovs[0] == vs[2] && ovs[1] == vs[0] && ovs[2] == vs[1]
                    || ovs[0] == vs[2] && ovs[1] == vs[1] && ovs[2] == vs[0]) {
                triangleList.remove(ovs);
                return;
            }
        }

        mf.updateTriangleList(triangleList.toArray());
    }

    public void deleteVertex() {
        if (currentRunMode == RunMode.AnimateEdit) {
            return;
        }
        if (selectedPoints.isEmpty()) {
            return;
        }
        for (Vector2 v : selectedPoints) {
            for (int tI = 0; tI < triangleList.size(); tI++) {
                Vector2[] ovs = triangleList.get(tI);
                if (ovs[0] == v || ovs[1] == v || ovs[2] == v) {
                    triangleList.remove(ovs);
                    tI--;
                }
            }
            points.remove(v);
        }

        mf.updateTriangleList(triangleList.toArray());
        mf.updateVertexList(points.toArray());

        selectedPoints.clear();
    }

    @Override
    public void create() {
        viewport = new ExtendViewport(480, 320);
        camera = new OrthographicCamera(480, 320);
        viewport.setCamera(camera);
        stage = new Stage(viewport);
        editStage = new Stage(viewport);

        renderer = new ShapeRenderer();

        font = new BitmapFont();
        fpsLable = new Label("", new Label.LabelStyle(font, Color.BLACK));

        stage.addActor(fpsLable);
        fpsLable.setPosition(10, 20);

        Gdx.input.setInputProcessor(stage);
        zoom = 1;

        polygonBatch = new PolygonSpriteBatch();

        stage.addListener(new InputListener() {
            float tmpX, tmpY;
            int touchButton;
            boolean isAppend = false;

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                switch (keycode) {
                    case Keys.CONTROL_LEFT:
                    case Keys.CONTROL_RIGHT:
                        isAppend = true;
                        break;
                }
                return super.keyDown(event, keycode); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                switch (keycode) {
                    case Keys.CONTROL_LEFT:
                    case Keys.CONTROL_RIGHT:
                        isAppend = false;
                        break;
                }
                return super.keyUp(event, keycode); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                if (currentRunMode == RunMode.VertexEdit) {
                    if (image != null) {
                        x = (x - image.getX()) / zoom;
                        y = (y - image.getY()) / zoom;
                        for (Vector2 vec : points) {
                            if (vec.dst(x, y) < 4) {
                                pointPoint = vec;
                                return true;
                            }
                        }
                    }
                    pointPoint = null;
                } else {
                    if (polygonSprite != null) {
                        x = (x - polygonSprite.getX()) / zoom;
                        y = (y - polygonSprite.getY()) / zoom;
                        for (Vector2 vec : pointsAnimation) {
                            if (vec.dst(x, y) < 4) {
                                pointPoint = vec;
                                return true;
                            }
                        }
                    }
                    pointPoint = null;
                }
                return super.mouseMoved(event, x, y); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if (currentRunMode == RunMode.VertexEdit) {
                    if (touchButton == Buttons.RIGHT) {
                        if (image != null) {
                            image.moveBy(x - tmpX, y - tmpY);
                        }
                    } else if (touchButton == Buttons.LEFT) {
                        if (!selectedPoints.isEmpty() && selectedPoints.size() > 1 && !isAppend) {
                            if (currentRunMode == RunMode.VertexEdit) {
                                for (Vector2 vector2 : selectedPoints) {
                                    vector2.add((x - tmpX) / zoom, (y - tmpY) / zoom);
                                }
                            } else {
                                for (int i = 0; i < selectedPoints.size(); i++) {
                                    Vector2 vector2 = selectedPoints.get(i);
                                    vector2.add((x - tmpX) / zoom, (y - tmpY) / zoom);

                                    vertices[i * 5] += (x - tmpX) / zoom;
                                    vertices[i * 5 + 1] += (y - tmpY) / zoom;
                                }
                            }
                        } else if (pointPoint != null) {
                            pointPoint.add((x - tmpX) / zoom, (y - tmpY) / zoom);
                            if (currentRunMode == RunMode.AnimateEdit) {
                                int i = points.indexOf(pointPoint);
                                vertices[i * 5] += (x - tmpX) / zoom;
                                vertices[i * 5 + 1] += (y - tmpY) / zoom;
                            }
                        } else if (selectRect != null) {
                            selectRect.width += x - tmpX;
                            selectRect.height += y - tmpY;
                        }
                    }
                } else {
                    if (touchButton == Buttons.RIGHT) {
                        if (polygonSprite != null) {
                            polygonSprite.translate(x - tmpX, y - tmpY);
                        }
                    } else if (touchButton == Buttons.LEFT) {
                        if (!selectedPointsAnimation.isEmpty() && selectedPointsAnimation.size() > 1 && !isAppend) {
                            for (int i = 0; i < selectedPointsAnimation.size(); i++) {
                                Vector2 vector2 = selectedPointsAnimation.get(i);
                                int p = pointsAnimation.indexOf(vector2);
                                vector2.add((x - tmpX) / zoom, (y - tmpY) / zoom);

                                vertices[p * 5] += (x - tmpX) / zoom;
                                vertices[p * 5 + 1] += (y - tmpY) / zoom;

                                frames[p * 2].addFrame(tf.currentFrameIndex, vertices[p * 5]);
                                frames[p * 2 + 1].addFrame(tf.currentFrameIndex, vertices[p * 5 + 1]);
                                tf.repaint();
                                if (maxFrameIndex < tf.currentFrameIndex) {
                                    maxFrameIndex = tf.currentFrameIndex;
                                }
                            }
                        } else if (pointPoint != null) {
                            pointPoint.add((x - tmpX) / zoom, (y - tmpY) / zoom);
                            int i = pointsAnimation.indexOf(pointPoint);
                            vertices[i * 5] += (x - tmpX) / zoom;
                            vertices[i * 5 + 1] += (y - tmpY) / zoom;

                            frames[i * 2].addFrame(tf.currentFrameIndex, vertices[i * 5]);
                            frames[i * 2 + 1].addFrame(tf.currentFrameIndex, vertices[i * 5 + 1]);
                            tf.repaint();
                            if (maxFrameIndex < tf.currentFrameIndex) {
                                maxFrameIndex = tf.currentFrameIndex;
                            }
                        } else if (selectRect != null) {
                            selectRect.width += x - tmpX;
                            selectRect.height += y - tmpY;
                        }
                    }
                }
                tmpX = x;
                tmpY = y;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (currentRunMode == RunMode.VertexEdit) {
                    if (selectRect != null && button == Buttons.LEFT) {
                        if (!isAppend) {
                            selectedPoints.clear();
                        }
                        selectRect.x = (selectRect.x - image.getX()) / zoom;
                        selectRect.y = (selectRect.y - image.getY()) / zoom;
                        selectRect.width = selectRect.width / zoom;
                        selectRect.height = selectRect.height / zoom;

                        if (selectRect.width < 0) {
                            selectRect.x += selectRect.width;
                            selectRect.width = 0 - selectRect.width;
                        }
                        if (selectRect.height < 0) {
                            selectRect.y += selectRect.height;
                            selectRect.height = 0 - selectRect.height;
                        }

                        //System.out.println(String.format("[%.2f,%.2f][%.2f,%.2f]", selectRect.x, selectRect.x + selectRect.width, selectRect.y, selectRect.y + selectRect.height));
                        for (Vector2 vec : points) {
                            //System.out.println(String.format("P[%.2f,%.2f]", vec.x, vec.y));
                            if (selectRect.contains(vec) && !selectedPoints.contains(vec)) {
                                selectedPoints.add(vec);
                            }
                        }
                        selectRect = null;
                    } else if (button == Buttons.LEFT) {
                        if (pointPoint == null) {
                            selectedPoints.clear();
                        } else {
                            if (!isAppend) {
                                selectedPoints.clear();
                                selectedPoints.add(pointPoint);
                            } else if (!selectedPoints.contains(pointPoint)) {
                                selectedPoints.add(pointPoint);
                            }
                        }
                    }
                } else {
                    if (selectRect != null && button == Buttons.LEFT) {
                        if (!isAppend) {
                            selectedPoints.clear();
                        }
                        selectRect.x = (selectRect.x - polygonSprite.getX()) / zoom;
                        selectRect.y = (selectRect.y - polygonSprite.getY()) / zoom;
                        selectRect.width = selectRect.width / zoom;
                        selectRect.height = selectRect.height / zoom;

                        if (selectRect.width < 0) {
                            selectRect.x += selectRect.width;
                            selectRect.width = 0 - selectRect.width;
                        }
                        if (selectRect.height < 0) {
                            selectRect.y += selectRect.height;
                            selectRect.height = 0 - selectRect.height;
                        }

                        for (Vector2 vec : pointsAnimation) {
                            if (selectRect.contains(vec) && !selectedPointsAnimation.contains(vec)) {
                                selectedPointsAnimation.add(vec);
                            }
                        }
                        selectRect = null;
                    } else if (button == Buttons.LEFT) {
                        if (pointPoint == null) {
                            selectedPointsAnimation.clear();
                        } else {
                            if (!isAppend) {
                                selectedPointsAnimation.clear();
                                selectedPointsAnimation.add(pointPoint);
                            } else if (!selectedPointsAnimation.contains(pointPoint)) {
                                selectedPointsAnimation.add(pointPoint);
                            }
                        }
                    }
                }
                super.touchUp(event, x, y, pointer, button); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                touchButton = button;
                tmpX = x;
                tmpY = y;

                if (currentRunMode == RunMode.VertexEdit) {

                    if (currentModeType == PointEditModeType.AddVertex && button == Buttons.LEFT && image != null) {
                        x = (x - image.getX()) / zoom;
                        y = (y - image.getY()) / zoom;
                        System.out.println("AddVertex.");
                        for (Vector2 vec : points) {
                            if (vec.dst(x, y) < 2) {
                                System.out.println("Near vertex exsit.");
                                return false;
                            }
                        }
                        points.add(new Vector2(x, y));

                        mf.updateVertexList(points.toArray());

                        System.out.println(String.format("Added Vertex[%s,%s]", x, y));
                    } else if (currentModeType == PointEditModeType.Normal && button == Buttons.LEFT && pointPoint != null) {
                        if (!isAppend) {
                            selectedPoints.clear();
                            selectedPoints.add(pointPoint);
                        } else if (!selectedPoints.contains(pointPoint)) {
                            selectedPoints.add(pointPoint);
                        }
                    } else if (currentModeType == PointEditModeType.Normal && button == Buttons.LEFT) {
                        selectRect = new Rectangle(x, y, 0, 0);
                    }
                } else {
                    if (currentModeType == PointEditModeType.Normal && button == Buttons.LEFT && pointPoint != null) {
                        if (!isAppend) {
                            selectedPointsAnimation.clear();
                            selectedPointsAnimation.add(pointPoint);
                        } else if (!selectedPointsAnimation.contains(pointPoint)) {
                            selectedPointsAnimation.add(pointPoint);
                        }
                    } else if (currentModeType == PointEditModeType.Normal && button == Buttons.LEFT) {
                        selectRect = new Rectangle(x, y, 0, 0);
                    }
                }
                return true;
            }

            @Override
            public boolean scrolled(InputEvent event, float x, float y, int amount) {
                //System.out.println(String.format("IE[%s] x[%s]y[%s] amount[%s]", event, x, y, amount));
                if (currentRunMode == RunMode.VertexEdit) {
                    if (image != null) {
                        //Still has some problem..
                        //image.setOrigin((x - image.getX()), (y - image.getY()));
                        if (amount > 0) {
                            zoom += 0.1f;
                        } else {
                            zoom -= 0.1f;
                            if (zoom < 0.1f) {
                                zoom = 0.1f;
                            }
                        }
                        image.setScale(zoom);
                        //image.moveBy(image.getWidth() * 0.1f, image.getHeight() * 0.1f);
                    }
                } else {
                    if (polygonSprite != null) {
                        //Still has some problem..
                        //image.setOrigin((x - image.getX()), (y - image.getY()));
                        if (amount > 0) {
                            zoom += 0.1f;
                        } else {
                            zoom -= 0.1f;
                            if (zoom < 0.1f) {
                                zoom = 0.1f;
                            }
                        }
                        polygonSprite.setScale(zoom);
                        //image.moveBy(image.getWidth() * 0.1f, image.getHeight() * 0.1f);
                    }
                }
                return super.scrolled(event, x, y, amount); //To change body of generated methods, choose Tools | Templates.
            }

        });

        renderer.setColor(Color.BLUE);
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glClearColor(1, 1, 1, 1);

        if (currentRunMode == RunMode.VertexEdit) {
            editStage.act();
            editStage.draw();

            renderer.setProjectionMatrix(camera.combined);
            renderer.begin(ShapeRenderer.ShapeType.Filled);
            for (Vector2 vec : points) {
                if (pointPoint == vec) {
                    renderer.setColor(Color.GREEN);
                    renderer.circle(vec.x * zoom + image.getX(), vec.y * zoom + image.getY(), 6);
                    renderer.setColor(Color.BLUE);
                }
                if (selectedPoints.contains(vec)) {
                    renderer.setColor(Color.RED);
                    renderer.circle(vec.x * zoom + image.getX(), vec.y * zoom + image.getY(), 5);
                    renderer.setColor(Color.BLUE);
                }
                renderer.circle(vec.x * zoom + image.getX(), vec.y * zoom + image.getY(), 4);
            }
            renderer.end();

            renderer.begin(ShapeRenderer.ShapeType.Line);
            for (Vector2[] vs : triangleList) {
                renderer.triangle(vs[0].x * zoom + image.getX(), vs[0].y * zoom + image.getY(), vs[1].x * zoom + image.getX(), vs[1].y * zoom + image.getY(), vs[2].x * zoom + image.getX(), vs[2].y * zoom + image.getY());
            }
            renderer.end();

            if (selectRect != null) {
                renderer.setColor(Color.ORANGE);
                renderer.begin(ShapeRenderer.ShapeType.Line);
                renderer.rect(selectRect.x, selectRect.y, selectRect.width, selectRect.height);
                renderer.end();
                renderer.setColor(Color.BLUE);
            }

        } else {

            if (isPlayingAnimation) {
                if (tf.currentFrameIndex < maxFrameIndex) {
                    tf.changeCurrentFrameIndex(++tf.currentFrameIndex);
                } else {
                    if (isLoop) {
                        tf.currentFrameIndex = 0;
                    } else {
                        isPlayingAnimation = false;
                    }
                }
            }

            polygonBatch.setProjectionMatrix(camera.combined);
            polygonBatch.begin();
            polygonSprite.draw(polygonBatch);
            polygonBatch.end();

            if (!isPlayingAnimation) {
                renderer.setProjectionMatrix(camera.combined);
                renderer.begin(ShapeRenderer.ShapeType.Filled);
                for (Vector2 vec : pointsAnimation) {
                    if (pointPoint == vec) {
                        renderer.setColor(Color.GREEN);
                        renderer.circle(vec.x * zoom + polygonSprite.getX(), vec.y * zoom + polygonSprite.getY(), 6);
                        renderer.setColor(Color.BLUE);
                    }
                    if (selectedPointsAnimation.contains(vec)) {
                        renderer.setColor(Color.RED);
                        renderer.circle(vec.x * zoom + polygonSprite.getX(), vec.y * zoom + polygonSprite.getY(), 5);
                        renderer.setColor(Color.BLUE);
                    }
                    renderer.circle(vec.x * zoom + polygonSprite.getX(), vec.y * zoom + polygonSprite.getY(), 4);
                }
                renderer.end();
            }
//            renderer.begin(ShapeRenderer.ShapeType.Line);
//            for (Vector2[] vs : triangleList) {
//                renderer.triangle(vs[0].x * zoom + polygonSprite.getX(), vs[0].y * zoom + polygonSprite.getY(), vs[1].x * zoom + polygonSprite.getX(), vs[1].y * zoom + polygonSprite.getY(), vs[2].x * zoom + polygonSprite.getX(), vs[2].y * zoom + polygonSprite.getY());
//            }
//            renderer.end();
            if (selectRect != null) {
                renderer.setColor(Color.ORANGE);
                renderer.begin(ShapeRenderer.ShapeType.Line);
                renderer.rect(selectRect.x, selectRect.y, selectRect.width, selectRect.height);
                renderer.end();
                renderer.setColor(Color.BLUE);
            }
        }

        stage.act();
        stage.draw();

        fpsLable.setText(String.format("FPS:%d\nZoom:%.2f", Gdx.graphics.getFramesPerSecond(), zoom));
    }

    @Override
    public void dispose() {
        font.dispose();
        stage.dispose();
        if (tex != null) {
            tex.dispose();
        }
        editStage.dispose();
        renderer.dispose();
        polygonBatch.dispose();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.position.x = width / 2;
        camera.position.y = height / 2;
        camera.update();
    }

}
