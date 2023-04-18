
package federate;

import execution.ReflectorRoverExecution;
import execution.SimulationEntityExecution;
import interaction.Interaction;
import skf.config.Configuration;
import skf.core.SEEAbstractFederateAmbassador;
import skf.model.interaction.annotations.InteractionClass;
import state.ReflectorRoverState;
import interaction.ReflectorLocationRequest;
import interaction.ReflectorLocationResponse;
import interaction.ReflectorInteractionRequest;
import interaction.ReflectorInteractionResponse;

/*
 * @author Will English
 * Based on ProspectingRover by Khris
 * 
 *  */
public class ReflectorRoverFederate extends SimulationEntityFederate {

    private ReflectorLocationRequest reflectorLocationRequest;
    private ReflectorLocationResponse reflectorLocationResponse;
    private ReflectorInteractionRequest reflectorInteractionRequest;
    private ReflectorInteractionResponse reflectorInteractionResponse;
    private ChargeConnectResponse chargeConnectResponse;

    public ReflectorRoverFederate(SEEAbstractFederateAmbassador ambassador, Configuration configuration) {
        super(ambassador, configuration);
    }

    @Override
    protected SimulationEntityExecution initializeExecution() {
    	ReflectorRoverState roverState = new ReflectorRoverState(getRandomHLAId(), 2, 2, 35, 35, 1.0, 2.0, 5.0);
        roverState.standby();
    	execution = new ReflectorRoverExecution(roverState);
        return  execution;
    }

    @Override
    protected void initializeInteractions() {
        super.initializeInteractions();
        
        reflectorLocationRequest = new ReflectorLocationRequest();
        reflectorLocationResponse = new ReflectorLocationResponse();
        reflectorInteractionRequest = new ReflectorInteractionRequest();
        reflectorInteractionResponse = new ReflectorInteractionResponse();
    }

    @Override
    protected void initialPublishSubscribe() {
        super.initialPublishSubscribe();

        boolean initialized = false;

        try {
            super.publishInteraction(reflectorLocationRequest);
            super.publishInteraction(reflectorLocationResponse);
            super.publishInteraction(reflectorInteractionRequest);
            super.publishInteraction(reflectorInteractionResponse);

            super.subscribeInteraction((Class<? extends InteractionClass>) ReflectorInteractionRequest.class);
            super.subscribeInteraction((Class<? extends InteractionClass>) ReflectorInteractionResponse.class);

            super.publishElement(execution.simulationEntityState, "ReflectorRover");

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

        ReflectorRoverExecution roverExecution = (ReflectorRoverExecution) this.execution;

        if(interaction instanceof ReflectorLocationResponse) {
        	ReflectorLocationResponse response = (ReflectorLocationResponse) interaction;
            logger.debug("Received Reflector Location Response " + response.getReflectorSite());
            roverExecution.receiveSelectReflectorLocationResponse(response.getReflectorSite());
            roverExecution.sendPathFindingInteraction(response.getReflectorSite());

            //For sunlight
        }else if(interaction instanceof ReflectorInteractionResponse) {
            ReflectorInteractionResponse response = (ReflectorInteractionResponse) interaction;
            logger.debug("Received Reflector Interaction Response " + response.getSunlight());
            roverExecution.receiveReflectorInteractionResponse(response.getSunlight());
        }else if (interaction instanceof ChargeConnectResponse){
			chargeConnectResponse.setCr(((ChargeConnectResponse) interaction).getCr());
			chargeConnectResponse.setFromHLAId(interaction.getFromHLAId());
			chargeConnectResponse.setToHLAId(interaction.getToHLAId());
			super.updateInteraction(chargeConnectResponse);
		}
    }

    @Override
    protected void sendInteractions() {
        super.sendInteractions();

        try {
            super.updateElement(execution.simulationEntityState);

            for(Interaction interaction : execution.interactions) {

                if(interaction instanceof ReflectorLocationRequest) {
                	ReflectorLocationRequest request = (ReflectorLocationRequest) interaction;
                	reflectorLocationRequest.setFromHLAId(request.getFromHLAId());
                	reflectorLocationRequest.setToHLAId(request.getToHLAId());

                    logger.debug("Sending Reflector Location Request " + reflectorLocationRequest.getFromHLAId());
                    super.updateInteraction(reflectorLocationRequest);
                    execution.interactions.remove(interaction);

                }else if(interaction instanceof  ReflectorInteractionRequest) {
                	ReflectorInteractionRequest request = (ReflectorInteractionRequest) interaction;
                    reflectorInteractionRequest.setFromHLAId(request.getFromHLAId());
                    reflectorInteractionRequest.setToHLAId(request.getToHLAId());

                    logger.debug("Sending Reflector Interaction Request " + reflectorInteractionRequest.getFromHLAId());
                    super.updateInteraction(reflectorInteractionRequest);
                    execution.interactions.remove(interaction);
                }else if(interaction instanceof ChargeConnectRequest){
                	 logger.debug("Sending Reflector Connect Request ");
        			((ReflectorRoverExecution) execution).receiveChargeConnectRequest(((ChargeConnectRequest) arg).getFromHLAId(), ((ChargeConnectRequest) arg).getLocation());
        		} else if (interaction instanceof ChargeDisconnectRequest){
        			logger.debug("Sending Reflector Connect Request ");
        			((ReflectorRoverExecution) execution).receiveChargeDisconnectRequest(((ChargeDisconnectRequest) arg).getFromHLAId());
        		}
            }

        }catch (Exception e) {
            e.printStackTrace();
        }

    }
}