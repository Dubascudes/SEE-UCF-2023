package federate;

import execution.ProspectingRoverExecution;
import execution.SimulationEntityExecution;
import execution.SolarPanelExecution;
import interaction.Interaction;
import interaction.PanelStatusRequest;
import skf.config.Configuration;
import skf.core.SEEAbstractFederateAmbassador;
import skf.model.interaction.annotations.InteractionClass;
import state.ProspectingRoverState;
import state.SolarPanelState;
import interaction.ProspectingLocationRequest;
import interaction.ProspectingLocationResponse;
import interaction.TowerStatusRequest;
import interaction.ProspectingInteractionRequest;
import interaction.ProspectingInteractionResponse;

/**
 * Created by Louis
 */
public class SolarPanelFederate extends SimulationEntityFederate {

    private PanelStatusRequest panelStatusRequest;

    public SolarPanelFederate(SEEAbstractFederateAmbassador ambassador, Configuration configuration) {
        super(ambassador, configuration);
    }

    @Override
    protected SimulationEntityExecution initializeExecution() {
        SolarPanelState panelState = new SolarPanelState(getRandomHLAId(), 20, 20, 7, 19, 100.0, 5.0, 5, 60);
        execution = new SolarPanelExecution(panelState);
        return execution;
    }

    @Override
    protected void initializeInteractions() {
        super.initializeInteractions();

        panelStatusRequest = new PanelStatusRequest();
    }

    @Override
    protected void initialPublishSubscribe() {
        super.initialPublishSubscribe();

        boolean initialized = false;
        
        PanelStatusRequest response = new PanelStatusRequest();
		response.setOutput(0.00);
		logger.debug("Received charge amount response" + response.isOutput());
		
        try {
            super.publishInteraction(panelStatusRequest);

            super.subscribeInteraction((Class<? extends InteractionClass>) PanelStatusRequest.class);

            super.publishElement(execution.simulationEntityState, "SolarPanel");

            initialized = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(!initialized){
                logger.fatal("Unable to complete initial publish/subscribe.");
                System.exit(-1);
            }
        }

    }

    @Override
    public void handleInteraction(Interaction interaction) {
        super.handleInteraction(interaction);

        SolarPanelExecution panelExecution = (SolarPanelExecution) this.execution;

        if(interaction instanceof PanelStatusRequest) {
        	PanelStatusRequest response = (PanelStatusRequest) interaction;
        	logger.debug("Received Panel Location Response " + response.getPosition());
        	//if statement here to determine if we are acquiring sunlight
        	//can change getAmount method to function to calculate actual electrical output based on panel efficiency and dimensions
        	response.setReceiving(true);
        	if(response.isReceiving() == true) {
        		response.setOutput(10000.00);
        		logger.debug("Received charge amount response" + response.isOutput());
        	}
        }
    }

    @Override
    protected void sendInteractions() {
        super.sendInteractions();

        try {
            super.updateElement(execution.simulationEntityState);

            for(Interaction interaction : execution.interactions) {
                // If statements to check every instance of an interaction
                if (interaction instanceof PanelStatusRequest){
                    PanelStatusRequest r = (PanelStatusRequest) interaction;
                    panelStatusRequest.setFromHLAId(r.getFromHLAId());
                    panelStatusRequest.setToHLAId(r.getToHLAId());
                    panelStatusRequest.setPosition(r.getPosition());
                    panelStatusRequest.setTime(getSimulationTime());

                    logger.debug("Sending PanelStatusRequest ID " + panelStatusRequest.getFromHLAId());
                    super.updateInteraction(panelStatusRequest);
                    execution.interactions.remove(interaction);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
