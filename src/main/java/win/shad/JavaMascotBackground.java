package win.shad;

import org.teavm.jso.JSBody;
import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.MouseEvent;
import org.teavm.jso.dom.events.TouchEvent;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLImageElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JavaMascotBackground {
    private static final int NUM_MASCOTS = 1000;
    private static final Random random = new Random();
    private static final double MAX_SPEED = 2.0;

    private boolean isDragging = false;
    private double lastX;
    private double lastY;
    private final double FORCE_RADIUS = 150;
    private final double FORCE_MULTIPLIER = 0.3;

    private HTMLCanvasElement canvas;
    private CanvasRenderingContext2D ctx;
    private HTMLImageElement baseImage;
    private final List<JavaMascot> mascots = new ArrayList<>();

    public void start() {
        setupCanvas();
        loadImage();
        setupResizeHandler();
        setupInputHandlers();
    }

    private void setupCanvas() {
        canvas = (HTMLCanvasElement) HTMLDocument.current().createElement("canvas");
        HTMLDocument.current().getBody().appendChild(canvas);
        resizeCanvas();
        ctx = (CanvasRenderingContext2D) canvas.getContext("2d");
    }

    private void loadImage() {
        HTMLImageElement img = (HTMLImageElement) HTMLDocument.current().createElement("img");
        img.setSrc("img.png");
        img.addEventListener("load", evt -> {
            baseImage = img;
            initMascots();
            animate();
        });
    }

    private void initMascots() {
        for (int i = 0; i < NUM_MASCOTS; i++) {
            mascots.add(new JavaMascot(
                    random.nextDouble() * canvas.getWidth(),
                    random.nextDouble() * canvas.getHeight(),
                    30 + random.nextInt(70),
                    30 + random.nextInt(70),
                    (random.nextDouble() - 0.5) * 0.02,
                    (random.nextDouble() - 0.5) * MAX_SPEED,
                    (random.nextDouble() - 0.5) * MAX_SPEED,
                    baseImage
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
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        for (JavaMascot mascot : mascots) {
            mascot.x += mascot.velocityX;
            mascot.y += mascot.velocityY;

            if (mascot.x < 0 || mascot.x > width) {
                mascot.velocityX *= -1;
                mascot.x = Math.max(0, Math.min(mascot.x, width));
            }

            if (mascot.y < 0 || mascot.y > height) {
                mascot.velocityY *= -1;
                mascot.y = Math.max(0, Math.min(mascot.y, height));
            }

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

    private void setupResizeHandler() {
        Window.current().addEventListener("resize", evt -> {
            resizeCanvas();
            mascots.forEach(m -> {
                m.x = random.nextDouble() * canvas.getWidth();
                m.y = random.nextDouble() * canvas.getHeight();
            });
        });
    }

    private void resizeCanvas() {
        canvas.setWidth(Window.current().getInnerWidth());
        canvas.setHeight(Window.current().getInnerHeight() - 1);
    }


    private void setupInputHandlers() {
        canvas.addEventListener("mousedown", this::handleMouseDown);
        canvas.addEventListener("mousemove", this::handleMouseMove);
        canvas.addEventListener("mouseup", this::handleMouseUp);
        canvas.addEventListener("mouseleave", this::handleMouseUp);

        canvas.addEventListener("touchstart", this::handleTouchStart);
        canvas.addEventListener("touchmove", this::handleTouchMove);
        canvas.addEventListener("touchend", this::handleTouchEnd);
        canvas.addEventListener("touchcancel", this::handleTouchEnd);
    }

    private void handleMouseDown(Event e) {
        MouseEvent evt = (MouseEvent) e;
        isDragging = true;
        lastX = evt.getClientX() - canvas.getOffsetLeft();
        lastY = evt.getClientY() - canvas.getOffsetTop();
        evt.preventDefault();
    }

    private void handleMouseMove(Event e) {
        if (!isDragging) return;

        MouseEvent evt = (MouseEvent) e;

        double currentX = evt.getClientX() - canvas.getOffsetLeft();
        double currentY = evt.getClientY() - canvas.getOffsetTop();
        applyForce(lastX, lastY, currentX, currentY);

        lastX = currentX;
        lastY = currentY;
        evt.preventDefault();
    }

    private void handleMouseUp(Event e) {
        MouseEvent evt = (MouseEvent) e;

        isDragging = false;
        evt.preventDefault();
    }

    private void handleTouchStart(Event e) {
        TouchEvent evt = (TouchEvent) e;

        if (evt.getTouches().getLength() > 0) {
            isDragging = true;
            lastX = evt.getTouches().get(0).getClientX() - canvas.getOffsetLeft();
            lastY = evt.getTouches().get(0).getClientY() - canvas.getOffsetTop();
        }
        evt.preventDefault();
    }

    private void handleTouchMove(Event e) {
        TouchEvent evt = (TouchEvent) e;

        if (!isDragging || evt.getTouches().getLength() == 0) return;

        double currentX = evt.getTouches().get(0).getClientX() - canvas.getOffsetLeft();
        double currentY = evt.getTouches().get(0).getClientY() - canvas.getOffsetTop();
        applyForce(lastX, lastY, currentX, currentY);

        lastX = currentX;
        lastY = currentY;
        evt.preventDefault();
    }

    private void handleTouchEnd(Event e) {
        TouchEvent evt = (TouchEvent) e;

        isDragging = false;
        evt.preventDefault();
    }

    private void applyForce(double fromX, double fromY, double toX, double toY) {
        double deltaX = toX - fromX;
        double deltaY = toY - fromY;

        for (JavaMascot mascot : mascots) {
            double dx = mascot.x - fromX;
            double dy = mascot.y - fromY;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < FORCE_RADIUS) {
                double force = (FORCE_RADIUS - distance) / FORCE_RADIUS;
                mascot.velocityX += deltaX * force * FORCE_MULTIPLIER;
                mascot.velocityY += deltaY * force * FORCE_MULTIPLIER;
            }
        }
    }

    static class JavaMascot {
        double x;
        double y;
        double width;
        double height;
        double speed;
        double angle;
        double velocityX;
        double velocityY;
        HTMLImageElement image;

        JavaMascot(double x, double y, double width, double height,
                   double rotationSpeed, double velocityX, double velocityY,
                   HTMLImageElement image) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.speed = rotationSpeed;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.image = image;
        }

        void update() {
            angle += speed;
        }
    }

    @JSBody(script = "return new CanvasRenderingContext2D();")
    private static native CanvasRenderingContext2D createContext();
}