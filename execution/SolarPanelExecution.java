package execution;

import interaction.ChargeAmountResponse;
import interaction.Interaction;
import interaction.PanelStatusRequest;
import interaction.ProspectingInteractionRequest;
import interaction.ProspectingLocationRequest;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import state.ChargeableEntityState;
import state.ProspectingRoverState;
import state.SimulationEntityState;

/**
 * Created by Louis
 */
public class SolarPanelExecution extends SimulationEntityExecution {

    private SimulationEntityState.GridIndex location = null;

    final static Logger logger = Logger.getLogger(SolarPanelExecution.class);

    public SolarPanelExecution(SimulationEntityState entityState) {
        super(entityState);
    }

    
    @Override
    public void activeEntityUpdate(){
    	SimulationEntityState panelState = (SimulationEntityState) this.simulationEntityState;
    }

    @Override
    public void staticUpdate() {
    	SimulationEntityState panelState = (SimulationEntityState) this.simulationEntityState;
        sendStatusRequest();
    }

    public SimulationEntityState.GridIndex getLocation() {
        return location;
    }

    public void setLocation(SimulationEntityState.GridIndex location) {
        this.location = location;
    }

    public void sendStatusRequest() {
    	interactions.add(new PanelStatusRequest(simulationEntityState.identifier, simulationEntityState.gridIndex));
    }
    /*
	public void sendChargeAmount(){
		SimulationEntityState panelState = (SimulationEntityState) this.simulationEntityState;
		ArrayList<Long> ids = new ArrayList<Long>();

		for(Interaction itr : interactions){
			if(itr instanceof ChargeAmountResponse){
				ids.add(((ChargeAmountResponse) itr).getFromHLAId());
			}
		}
		for(Long id : panelState.chargingEntityQueue){
			if(!ids.contains(id)){
				interactions.add(new ChargeAmountResponse(simulationEntityState.identifier, id, panelState.getCharge(id)));
			}
		}
	}
    */
}
