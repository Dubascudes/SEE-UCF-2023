package state;

import org.apache.log4j.Logger;

import coder.SpaceTimeCoordinateStateCoder;
import model.AngularVelocity;
import model.AttitudeQuaternion;
import model.HLARotationalState;
import model.HLATranslationalState;
import model.Position;
import model.SpaceTimeCoordinateState;
import model.Velocity;
import skf.coder.HLAunicodeStringCoder;
import skf.model.object.annotations.Attribute;
import skf.model.object.annotations.ObjectClass;

@ObjectClass(name= "PhysicalEntity.SolarPanel")
public class TowerState extends SimulationEntityState {
    final static Logger logger = Logger.getLogger(TowerState.class);

    @Attribute(name = "parent_reference_frame", coder = HLAunicodeStringCoder.class)
    private String parentReferenceFrame = null;

    @Attribute(name="state", coder= SpaceTimeCoordinateStateCoder.class)
    private SpaceTimeCoordinateState state = null;

    public SolarPanelState(long identifier, int gridX, int gridY,
            int isruGridX, int isruGridY, double movementSpeed,
            double gridCellSize, int safeGridX, int safeGridY)
    {
        super(identifier, gridX, gridY, movementSpeed, gridCellSize);

        setParentReferenceFrame("SeeLunarSouthPoleBaseLocalView");


        this.position[0] = 1850;
        this.position[1] = -10;
        this.position[2] = -591;
        this.rotationalQuat.set(0.0f, 0.7071068f, 0.7071068f, 0.0f);

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
        translationalState.setVelocity(new Velocity(10,0,0));

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
}
