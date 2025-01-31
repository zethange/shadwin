package win.shad;

import org.teavm.jso.JSBody;
import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLImageElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JavaMascotBackground {
    private static final int NUM_MASCOTS = 1_000;
    private static final Random random = new Random();

    private HTMLCanvasElement canvas;
    private CanvasRenderingContext2D ctx;
    private final List<HTMLImageElement> images = new ArrayList<>();
    private final List<JavaMascot> mascots = new ArrayList<>();


    public void start() {
        canvas = (HTMLCanvasElement) HTMLDocument.current().createElement("canvas");
        HTMLDocument.current().getBody().appendChild(canvas);
        resizeCanvas();
        ctx = (CanvasRenderingContext2D) canvas.getContext("2d");

        loadImages();
    }

    private void loadImages() {
        String image = "img.png";

        for (int i = 0; i < NUM_MASCOTS; i++) {
            HTMLImageElement img = (HTMLImageElement) HTMLDocument.current().createElement("img");
            img.setSrc(image);
            img.addEventListener("load", evt -> {
                images.add(img);
                if (images.size() == NUM_MASCOTS) {
                    initMascots();
                    animate();
                }
            });
        }
    }

    private void initMascots() {
        for (int i = 0; i < NUM_MASCOTS; i++) {
            mascots.add(new JavaMascot(
                    random.nextDouble(1) * canvas.getWidth(),
                    random.nextDouble(1) * canvas.getHeight(),
                    30 + random.nextInt(70),
                    30 + random.nextInt(70),
                    (random.nextDouble() - 0.5) * 0.02,
                    images.get(random.nextInt(images.size()))
            ));
        }
    }

    private void animate() {
        Window.requestAnimationFrame(timestamp -> {
            update();
            draw();
            animate();
        });
    }

    private void update() {
        for (JavaMascot mascot : mascots) {
            mascot.update();
        }
    }

    private void draw() {
        ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (JavaMascot mascot : mascots) {
            ctx.save();

            ctx.translate(mascot.x, mascot.y);
            ctx.rotate(mascot.angle);

            ctx.drawImage(mascot.image,
                    -mascot.width / 2,
                    -mascot.height / 2,
                    mascot.width,
                    mascot.height
            );

            ctx.restore();
        }
    }

    @JSBody(script = "return new CanvasRenderingContext2D();")
    private static native CanvasRenderingContext2D createContext();

    private void resizeCanvas() {
        canvas.setWidth(Window.current().getInnerWidth());
        canvas.setHeight(Window.current().getInnerHeight() - 1);
    }

    static class JavaMascot {
        double x;
        double y;
        double width;
        double height;
        double speed;
        double angle;
        HTMLImageElement image;

        JavaMascot(double x, double y, double width, double height, double speed, HTMLImageElement image) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.speed = speed;
            this.image = image;
        }

        void update() {
            angle += speed;
        }
    }
}