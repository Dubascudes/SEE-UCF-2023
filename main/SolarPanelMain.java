package main;

import federate.RASSORFederate;
import federate.SEENullFederateAmbassador;
import federate.SimulationEntityFederate;
import federate.SolarPanelFederate;
import skf.core.SEEAbstractFederateAmbassador;
import skf.config.Configuration;
import skf.config.ConfigurationFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by Louis
 */
public class SolarPanelMain extends SimulationEntityMain {


    @Override
    public File getConfigFile() { return new File("conf/SolarPanelConf.json"); }

    @Override
    public SimulationEntityFederate initializeFederate() {

        SEEAbstractFederateAmbassador ambassador = new SEENullFederateAmbassador();
        ConfigurationFactory configurationFactory = new ConfigurationFactory();

        try{

            Configuration configuration = configurationFactory.importConfiguration(this.getConfigFile());

            federate = new SolarPanelFederate(ambassador, configuration);
            return federate;
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }


}
