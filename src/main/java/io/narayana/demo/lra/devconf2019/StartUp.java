package io.narayana.demo.lra.devconf2019;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.narayana.demo.lra.devconf2019.jpa.Flight;

@ApplicationScoped
public class StartUp {
    private static final Logger log = Logger.getLogger(StartUp.class);

    @Inject @ConfigProperty(name = "init.csv")
    private String pathToCsvInitFile;

    @Inject
    private FlightManager flighthManagement;

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        log.debug("startup routine was succesfully initialized");
        if(pathToCsvInitFile != null && !pathToCsvInitFile.isEmpty()) {
            log.infof("Going to load data from CSV file at '%s'", pathToCsvInitFile);
            loadCsv(pathToCsvInitFile);
        }
    }



    public void loadCsv(String pathString) {
        Path pathToCsv = Paths.get(pathString);
        if(!pathToCsv.toFile().isFile()) {
            log.warnf("Path '%s' is not correct path to a file. No data to load.", pathString);
            return;
        }
        try {
            Files.lines(pathToCsv).forEach(line -> {
                String[] lineSplit = line.split(";");
                if(lineSplit.length != 3) {
                    log.warnf("Cannot parse line '%s' from file '%s' as expecting to have"
                            + " 3 values to be loaded to DB", line, pathString);
                    return;
                }
                Flight flight = new Flight();
                try {
                    flight.setDate(new SimpleDateFormat(Flight.DATE_FORMAT).parse(lineSplit[0]));
                } catch (ParseException pe) {
                    log.warnf("Cannot parse line '%s' from file '%s' as parsing of date '%s' failed",
                        line, pathString, lineSplit[0]);
                    return;
                }
                try {
                    flight.setNumberOfSeats(Integer.parseInt(lineSplit[1]));
                    flight.setBookedSeats(Integer.parseInt(lineSplit[2]));
                } catch (NumberFormatException nfe) {
                    log.warnf("Cannot parse line '%s' from file '%s' as parsing of one from numbers '%s', '%s' failed",
                            line, pathString, lineSplit[1], lineSplit[2]);
                    return;
                }
                // save loaded csv data through jpa
                flighthManagement.save(flight);
                log.infof("Saved flight: %s", flight);
            });
        } catch(IOException ioe) {
            log.errorf(ioe, "Cannot load and process CSV file at '%s'", pathString);
            return;
        }
    }
}
