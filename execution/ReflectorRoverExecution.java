package execution;

import interaction.ReflectorInteractionRequest;
import interaction.ReflectorLocationRequest;
import model.Position;
import interaction.ReflectorInteractionRequest;
import interaction.ReflectorLocationRequest;
import org.apache.log4j.Logger;
import state.ChargeableEntityState;
import state.ReflectorRoverState;
import state.SimulationEntityState;
import state.SimulationEntityState.GridIndex;

/*
 * 
 * @author William English
 * Based on ProspectingRover by Khris
 */

public class ReflectorRoverExecution extends ChargeableEntityExecution {

    private SimulationEntityState.GridIndex location = null;

    final static Logger logger = Logger.getLogger(ProspectingRoverExecution.class);

    private int locationCounter = 0;

    public ReflectorRoverExecution(ChargeableEntityState entityState) {
        super(entityState);
    }

    @Override
    public void staticUpdate() {
        super.staticUpdate();
        ReflectorRoverState rover = (ReflectorRoverState) this.simulationEntityState;

        if(rover.getRoverState() == ReflectorRoverState.RoverState.WaitingToSelectReflectorLocation) {
            locationCounter++;

            if(locationCounter >= 20) {
                sendSelectReflectorLocationRequest();
            }
        }
    }
    
    @Override
    public void activeEntityUpdate() {

        ReflectorRoverState rover = (ReflectorRoverState) this.simulationEntityState;

        if(rover.getRoverState() == ReflectorRoverState.RoverState.Standby) {

            rover.standby();
            sendSelectReflectorLocationRequest();

        } else if(rover.getRoverState() == ReflectorRoverState.RoverState.Standby &&
                rover.chargeState == ChargeableEntityState.ChargeState.Null) {

            rover.standby();
            sendSelectReflectorLocationRequest();

        } else if (rover.getRoverState() == ReflectorRoverState.RoverState.MovingToReflectorLocation
                && rover.movementState == SimulationEntityState.MovementState.Stopped) {

            if(!(rover.gridIndex.row == rover.getReflectorIndex().col
                    && rover.gridIndex.col == rover.getReflectorIndex().row)) {
                logger.debug("Re-Pathfinding to reflecting location, something is blocking path");
                try {
                    rover.beginPathFinding(rover.getReflectorIndex().col, //originally (col, row)
                            rover.getReflectorIndex().row);
                } catch(AssertionError e) {
                    //  Something happened, try another locationq
                    rover.waitForReflectorInteraction();
                    sendReflectorInteractionRequest();
                }

            } else {

                rover.waitForReflectorInteraction();
                sendReflectorInteractionRequest();
            }

        } else if (rover.getRoverState() == ReflectorRoverState.RoverState.Reflecting) {

            rover.afterReflect();

        } else if(rover.getRoverState() == ReflectorRoverState.RoverState.HandleCharge &&
                 rover.chargeState == ChargeableEntityState.ChargeState.Null) {
            rover.standby();
        }

    }

    public void receiveSelectReflectorLocationResponse(GridIndex gridIndex) {

        ReflectorRoverState rover = (ReflectorRoverState) this.simulationEntityState;
        rover.selectReflectorLocationResponse(gridIndex.row, gridIndex.col);

    }

    //Getting sunlight
    public void receiveReflectorInteractionResponse(boolean sunlight) {
        ReflectorRoverState rover = (ReflectorRoverState) this.simulationEntityState;
        rover.reflectorInteractionResponse(sunlight);
    }

    public void sendSelectReflectorLocationRequest() {
        ReflectorLocationRequest interaction = new ReflectorLocationRequest(simulationEntityState.identifier);
        interactions.add(interaction);
    }

    public void sendReflectorInteractionRequest() {
        locationCounter = 0;
        ReflectorInteractionRequest interaction = new ReflectorInteractionRequest(simulationEntityState.identifier);
        interactions.add(interaction);
    }
    public void sendChargeConnectRequest() {
        ChargeConnectRequest interaction = new ChargeConnectRequest(simulationEntityState.identifier);
        interactions.add(interaction);
    }
    public void sendChargeDisconnectRequest() {
        ChargeDisconnectRequest interaction = new ChargeDisconnectRequest(simulationEntityState.identifier);
        interactions.add(interaction);
    }

    public SimulationEntityState.GridIndex getLocation() {
        return location;
    }

    public void setLocation(SimulationEntityState.GridIndex location) {
        this.location = location;
    }

}