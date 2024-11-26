package it.unibo.mvc;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        
        final Configuration.Builder builderConfig = new Configuration.Builder();

        try (InputStream input = DrawNumberApp.class.getResourceAsStream("/config.yml");
            BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            
            String lineString;
            while ((lineString = reader.readLine()) != null) {
                lineString = lineString.trim();
                if (lineString.startsWith("minimum:")) {
                    builderConfig.setMin(Integer.parseInt(lineString.split(":")[1].trim()));
                } else if (lineString.startsWith("maximum:")) {
                    builderConfig.setMax(Integer.parseInt(lineString.split(":")[1].trim()));
                } else if (lineString.startsWith("attempts:")) {
                    builderConfig.setAttempts(Integer.parseInt(lineString.split(":")[1].trim()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        final Configuration configuration = builderConfig.build();
        if (configuration.isConsistent()) {
            this.model = new DrawNumberImpl(configuration);
        } else {
            displayError("Inconsistent configuration: "
                + "min: " + configuration.getMin() + ", "
                + "max: " + configuration.getMax() + ", "
                + "attempts: " + configuration.getAttempts() + ". Using defaults instead.");
            this.model = new DrawNumberImpl(new Configuration.Builder().build());
        }
    }

    private void displayError(final String err) {
        for (final DrawNumberView view: views) {
            view.displayError(err);
        }
    }

    
    

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl());
    }

}
