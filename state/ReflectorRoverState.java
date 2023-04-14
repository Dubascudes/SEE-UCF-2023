package state;

import coder.HLAGridIndexCoder;
import coder.SpaceTimeCoordinateStateCoder;
import model.*;
import org.apache.log4j.Logger;
import skf.coder.HLAasciiStringCoder;
import skf.coder.HLAfloat64LECoder;
import skf.coder.HLAunicodeStringCoder;
import skf.model.object.annotations.Attribute;
import skf.model.object.annotations.ObjectClass;

import java.util.ArrayList;

/*
 * @author Will English
 */
@ObjectClass(name = "PhysicalEntity.ReflectorRover")

public class ReflectorRoverState extends SimpleChargeableEntityState {
    final static Logger logger = Logger.getLogger(ReflectorRoverState.class);

    public ReflectorRoverState() {
        super(-1L, 0,0,0,0,0,0,0);
    }

    public enum RoverState {
    		Standby,
            HandleCharge,
            WaitingToSelectReflectorLocation,
            MovingToReflectorLocation,
            WaitingReflectorInteraction,
            Reflecting
    }

    public static class ReflectorMapEntry {

        @Attribute(name = "index", coder = HLAGridIndexCoder.class)
        public GridIndex index;
        //GridIndex index;

        //Change for sunlight
        @Attribute(name = "sunlightPower", coder = HLAfloat64LECoder.class)
        public double sunlightPower;

        public ReflectorMapEntry(GridIndex index, double sunlightPower) {
            this.index= index;
            this.sunlightPower= sunlightPower;
        }

		public ReflectorMapEntry(GridIndex reflectorIndex, boolean sunlight) {
			// TODO Auto-generated constructor stub
		}
    }
    

    public GridIndex getReflectorIndex() {
        return reflectorIndex;
    }

    public void setReflectorIndex(GridIndex reflectorIndex) {
        this.reflectorIndex = reflectorIndex;
    }

    @Attribute(name="reflectorIndex", coder=HLAGridIndexCoder.class)
    private GridIndex reflectorIndex = new GridIndex( -1, -1);

    private ArrayList<ReflectorMapEntry> reflectorMap;

    /* ONLY USE FOR HLA, AVOIDS NEED FOR CODER */
    public String getRoverStateString() {
        return this.roverState.name();
    }

    /* ONLY USE FOR HLA, AVOIDS NEED FOR CODER */
    public void setRoverStateString(String state) {
        this.roverState = RoverState.valueOf(state);
    }

    @Attribute(name="reflectorRoverState", coder= HLAasciiStringCoder.class)
    public String roverStateString = null;

    private RoverState roverState = RoverState.Standby;

    @Attribute(name = "parent_reference_frame", coder = HLAunicodeStringCoder.class)
    private String parentReferenceFrame = null;

    @Attribute(name="state", coder= SpaceTimeCoordinateStateCoder.class)
    private SpaceTimeCoordinateState state = null;


    public ReflectorRoverState(long identifier, int gridX, int gridY, int isruGridX, int isruGridY,
                                 double capacity, double movementSpeed, double gridCellSize) {
        super(identifier, gridX, gridY, isruGridX, isruGridY, capacity, movementSpeed, gridCellSize);
        reflectorMap = new ArrayList<>();
        setParentReferenceFrame("AitkenBasinLocalFixed");
    }
    
    

    public void standby() {
        assert this.roverState == RoverState.Standby || this.roverState == RoverState.HandleCharge;

        if(this.needsCharge()){
            this.handleCharge();
            this.roverState = RoverState.HandleCharge;
            logger.debug("Reflector Rover state transition: Standby -> HandleCharge");
        } else if(this.chargeState == ChargeState.Null) {
            this.roverState = RoverState.WaitingToSelectReflectorLocation;
            logger.debug("Reflector Rover state transition: Standby -> WaitingToSelectReflectorLocation");
        }
    }

    public void selectReflectorLocationResponse(int selX, int selY) {
        assert  this.roverState == RoverState.WaitingToSelectReflectorLocation;
        reflectorIndex = new GridIndex(selY, selX);

        this.beginPathFinding(reflectorIndex.col, reflectorIndex.row);
        this.roverState = RoverState.MovingToReflectorLocation;
    }

    public void waitForReflectorInteraction() {
        assert this.roverState == RoverState.MovingToReflectorLocation;
        assert this.movementState == MovementState.Stopped;
        assert this.gridIndex.row == reflectorIndex.col && this.gridIndex.col == reflectorIndex.row;
        this.roverState = RoverState.WaitingReflectorInteraction;
        logger.debug("Reflector Rover state transition: MovingToReflectorLocation -> WaitingReflectorInteraction");

    }

    public void reflectorInteractionResponse(boolean sunlight) {
        assert this.roverState == RoverState.WaitingReflectorInteraction;

        ReflectorMapEntry entry = new ReflectorMapEntry(this.reflectorIndex, sunlight);
        this.reflectorMap.add(entry);

        this.roverState = RoverState.Reflecting;
        logger.debug("Reflector Rover state transition: WaitingReflectorInteraction -> Reflecting");
    }
    

    public void afterReflect() {
        assert this.roverState == RoverState.Reflecting;
        this.roverState = RoverState.Standby;
        logger.debug("Reflector Rover state transition: Reflecting -> Standby");
    }

    public ArrayList<ReflectorMapEntry> getReflectorMap() {
        return reflectorMap;
    }

    public RoverState getRoverState() {
        return roverState;
    }

    public void setRoverState(RoverState roverState) {
        this.roverState = roverState;
    }

    public String getParentReferenceFrame() {
        return parentReferenceFrame;
    }

    public void setParentReferenceFrame(String parentReferenceFrame) {
        this.parentReferenceFrame = parentReferenceFrame;
    }

    public SpaceTimeCoordinateState getState() {
        SpaceTimeCoordinateState state = new SpaceTimeCoordinateState();
        HLATranslationalState translationalState = new HLATranslationalState();
        HLARotationalState rotationalState = new HLARotationalState();

        translationalState.setPosition(new Position(position[0], position[1], position[2]));
        translationalState.setVelocity(new Velocity(0,0,0));

        rotationalState.setAttitudeQuaternion(new AttitudeQuaternion(rotationalQuat.w, rotationalQuat.x, rotationalQuat.y, rotationalQuat.z));
        rotationalState.setAngularVelocity(new AngularVelocity(0,0,0));

        state.setTranslationalState(translationalState);
        state.setRotationalState(rotationalState);
        state.setTime(0);

        return state;
    }

    public void setState(SpaceTimeCoordinateState state) {
        this.state = state;
    }

    @Override
    public boolean needsCharge() {
        return this.currentCharge < this.capacity * 0.7;
    }

    @Override
    public void useCharge() {
        if(this.chargeState != ChargeState.Charging) {
            if(this.movementState == MovementState.InMotion) {
                this.currentCharge = Math.max(0.0, this.currentCharge-0.01);
            }
        }
    }



}