package main;

import federate.ReflectorRoverFederate;
import federate.SEENullFederateAmbassador;
import federate.SimulationEntityFederate;
import skf.config.Configuration;
import skf.config.ConfigurationFactory;
import skf.core.SEEAbstractFederateAmbassador;

import java.io.File;
import java.io.IOException;

/*
 * @author Will English
 * Based on ProspectingRover by Khris
 */
public class ReflectorRoverMain extends SimulationEntityMain {


    @Override
    public File getConfigFile() {
        return new File("conf/ReflectorRoverConf.json");
    }

    @Override
    public SimulationEntityFederate initializeFederate() {

        SEEAbstractFederateAmbassador ambassador = new SEENullFederateAmbassador();
        ConfigurationFactory configurationFactory = new ConfigurationFactory();

        try {
            Configuration configuration = configurationFactory.
                                            importConfiguration(getConfigFile());
            federate = new ReflectorRoverFederate(ambassador, configuration);
            return federate;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


}
