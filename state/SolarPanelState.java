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
import skf.coder.HLAbooleanCoder;
import java.util.ArrayList;

/**
 * Created by Louis
 */
@ObjectClass(name = "PhysicalEntity.SolarPanel")
public class SolarPanelState extends SimpleChargeableEntityState {
    final static Logger logger = Logger.getLogger(SolarPanelState.class);

    public SolarPanelState() {
        super(-1L, 0,0,0,0,0,0,0);
    }

    public enum PanelState {
    		Standby,
            HandleCharge
    }

    public static class ProspectingMapEntry {
        @Attribute(name = "kwhElectricalOutput", coder = HLAfloat64LECoder.class)
        public double kwhElectricalOutput = 0;
        
        @Attribute(name = "mylarSolarInput", coder = HLAbooleanCoder.class)
        public Boolean mylarSolarInput = null;

    }

    public double getElectricalOutput() {
        return kwhElectricalOutput;
    }

    public void setkwhElectricalOutput(double output) {
        this.kwhElectricalOutput = output;
    }
    
    @Attribute(name="kwhElectricalOutput", coder=HLAfloat64LECoder.class)
    private double kwhElectricalOutput = 10000.00;

    @Attribute(name = "parent_reference_frame", coder = HLAunicodeStringCoder.class)
    private String parentReferenceFrame = null;

    @Attribute(name="state", coder= SpaceTimeCoordinateStateCoder.class)
    private SpaceTimeCoordinateState state = null;


    public SolarPanelState(long identifier, int gridX, int gridY, double capacity) {
        super(identifier, gridX, gridY, capacity);
        prospectingMap = new ArrayList<>();
        setParentReferenceFrame("AitkenBasinLocalFixed");
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
}
