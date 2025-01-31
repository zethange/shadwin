package win.shad;

import org.teavm.jso.JSBody;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

import java.util.Calendar;
import java.util.Date;

public class AleksandrShakhovWin {
    private static final double MILLIS_PER_YEAR = 1000 * 60 * 60 * 24 * 365.25;

    private static double getYearsDiff(Date start) {
        return (System.currentTimeMillis() - start.getTime()) / MILLIS_PER_YEAR;
    }

    public static void main(String[] args) {

        HTMLElement el = HTMLDocument.current().getElementById("count");
        Date startDate = new Date(2024 - 1900, Calendar.JANUARY, 1);


        Window.setInterval(() -> {
            double years = getYearsDiff(startDate);
            el.setInnerHTML(String.format("%.9f", years));
        }, 10);

        new JavaMascotBackground().start();
    }
}