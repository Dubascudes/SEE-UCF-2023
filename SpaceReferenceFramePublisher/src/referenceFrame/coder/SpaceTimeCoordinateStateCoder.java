package referenceFrame.coder;

import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAfixedArray;
import hla.rti1516e.encoding.HLAfixedRecord;
import hla.rti1516e.encoding.HLAfloat64LE;
import hla.rti1516e.exceptions.RTIinternalError;

import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.siso.spacefom.frame.ReferenceFrameRotation;
import org.siso.spacefom.frame.ReferenceFrameTranslation;
import org.siso.spacefom.frame.SpaceTimeCoordinateState;
import org.siso.spacefom.frame.time.FloatingPointTime;

import skf.coder.Coder;

public class SpaceTimeCoordinateStateCoder implements Coder<SpaceTimeCoordinateState> {

	private EncoderFactory factory = null;
	private HLAfixedRecord coder = null;

	//Translational State
	private HLAfixedRecord translationalCoder = null;
	private HLAfixedArray<HLAfloat64LE> positionVector = null;
	private HLAfixedArray<HLAfloat64LE> velocityVector = null;

	//Rotational State
	private HLAfixedRecord rotationalCoder = null;
	private HLAfixedRecord quaternionCoder = null;
	private HLAfloat64LE scalar;
	private HLAfixedArray<HLAfloat64LE> vector;
	private HLAfixedArray<HLAfloat64LE> angularVelocityVector = null;

	//Time
	private HLAfloat64LE time;

	public SpaceTimeCoordinateStateCoder() throws RTIinternalError {

		this.factory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();
		this.coder = factory.createHLAfixedRecord();

		//Translational State
		this.translationalCoder = factory.createHLAfixedRecord();
		this.positionVector = factory.createHLAfixedArray(factory.createHLAfloat64LE(), factory.createHLAfloat64LE(), factory.createHLAfloat64LE());
		this.velocityVector = factory.createHLAfixedArray(factory.createHLAfloat64LE(), factory.createHLAfloat64LE(), factory.createHLAfloat64LE());

		this.translationalCoder.add(positionVector);
		this.translationalCoder.add(velocityVector);


		//Rotational State
		this.rotationalCoder = factory.createHLAfixedRecord();

		this.quaternionCoder = factory.createHLAfixedRecord();
		this.scalar = factory.createHLAfloat64LE();
		this.vector = factory.createHLAfixedArray(factory.createHLAfloat64LE(), factory.createHLAfloat64LE(), factory.createHLAfloat64LE());
		this.quaternionCoder.add(scalar);
		this.quaternionCoder.add(vector);

		this.angularVelocityVector = factory.createHLAfixedArray(factory.createHLAfloat64LE(), factory.createHLAfloat64LE(), factory.createHLAfloat64LE());

		this.rotationalCoder.add(quaternionCoder);
		this.rotationalCoder.add(angularVelocityVector);

		//Time
		this.time = factory.createHLAfloat64LE();


		this.coder.add(translationalCoder);
		this.coder.add(rotationalCoder);
		this.coder.add(time);

	}

	@SuppressWarnings("unchecked")
	@Override
	public SpaceTimeCoordinateState decode(byte[] arg0) throws DecoderException {

		coder.decode(arg0);

		// ************** Translational State *********************
		HLAfixedRecord tr = (HLAfixedRecord) coder.get(0);
		double[] decodePosition = new double[3]; 
		decodePosition[0] = ((HLAfixedArray<HLAfloat64LE>) tr.get(0)).get(0).getValue();
		decodePosition[1] = ((HLAfixedArray<HLAfloat64LE>) tr.get(0)).get(1).getValue();
		decodePosition[2] = ((HLAfixedArray<HLAfloat64LE>) tr.get(0)).get(2).getValue();

		double[] decodeVelocity = new double[3]; 
		decodeVelocity[0] = ((HLAfixedArray<HLAfloat64LE>) tr.get(1)).get(0).getValue();
		decodeVelocity[1] = ((HLAfixedArray<HLAfloat64LE>) tr.get(1)).get(1).getValue();
		decodeVelocity[2] = ((HLAfixedArray<HLAfloat64LE>) tr.get(1)).get(2).getValue();

		// ************** Rotational State *********************
		HLAfixedRecord rot = (HLAfixedRecord) coder.get(1);
		HLAfixedRecord decodeQuaternion = (HLAfixedRecord) rot.get(0);
		double decodeScalar = ((HLAfloat64LE) decodeQuaternion.get(0)).getValue();
		double[] decodeVector = new double[3]; 
		decodeVector[0] = ((HLAfixedArray<HLAfloat64LE>) decodeQuaternion.get(1)).get(0).getValue();
		decodeVector[1] = ((HLAfixedArray<HLAfloat64LE>) decodeQuaternion.get(1)).get(1).getValue();
		decodeVector[2] = ((HLAfixedArray<HLAfloat64LE>) decodeQuaternion.get(1)).get(2).getValue();

		double[] decodeAngularVelocity = new double[3]; 
		decodeAngularVelocity[0] = ((HLAfixedArray<HLAfloat64LE>) rot.get(1)).get(0).getValue();
		decodeAngularVelocity[1] = ((HLAfixedArray<HLAfloat64LE>) rot.get(1)).get(1).getValue();
		decodeAngularVelocity[2] = ((HLAfixedArray<HLAfloat64LE>) rot.get(1)).get(2).getValue();

		// ************** Time *********************
		HLAfloat64LE t = (HLAfloat64LE) coder.get(2);
		double decodeTime = t.getValue();

		// ************** Build SpaceTimeCoordinateState object *********************
		SpaceTimeCoordinateState stcs = new SpaceTimeCoordinateState();

		ReferenceFrameTranslation translState = stcs.getTranslationalState();
		translState.setPosition(new Vector3D(decodePosition));
		translState.setVelocity(new Vector3D(decodeVelocity));

		ReferenceFrameRotation rotState = stcs.getRotationState();
		rotState.setAttitudeQuaternion(new Quaternion(decodeScalar, decodeVector));
		rotState.setAngularVelocityVector(new Vector3D(decodeAngularVelocity));

		FloatingPointTime timeState = (FloatingPointTime) stcs.getTime();
		timeState.setValue(decodeTime);

		return stcs;
	}

	@Override
	public byte[] encode(SpaceTimeCoordinateState arg0) {

		// ************** Translational State *********************
		this.positionVector.get(0).setValue(arg0.getTranslationalState().getPosition().getX());
		this.positionVector.get(1).setValue(arg0.getTranslationalState().getPosition().getY());
		this.positionVector.get(2).setValue(arg0.getTranslationalState().getPosition().getZ());

		this.velocityVector.get(0).setValue(arg0.getTranslationalState().getVelocity().getX());
		this.velocityVector.get(1).setValue(arg0.getTranslationalState().getVelocity().getY());
		this.velocityVector.get(2).setValue(arg0.getTranslationalState().getVelocity().getZ());

		// ************** Rotational State *********************
		this.scalar.setValue(arg0.getRotationState().getAttitudeQuaternion().getQ0());
		this.vector.get(0).setValue(arg0.getRotationState().getAttitudeQuaternion().getQ1());
		this.vector.get(1).setValue(arg0.getRotationState().getAttitudeQuaternion().getQ2());
		this.vector.get(2).setValue(arg0.getRotationState().getAttitudeQuaternion().getQ3());

		this.angularVelocityVector.get(0).setValue(arg0.getRotationState().getAngularVelocityVector().getX());
		this.angularVelocityVector.get(1).setValue(arg0.getRotationState().getAngularVelocityVector().getY());
		this.angularVelocityVector.get(2).setValue(arg0.getRotationState().getAngularVelocityVector().getZ());
		
		// ************** Time *********************
		this.time.setValue(arg0.getTime().getValue().doubleValue());
		
		return coder.toByteArray();
	}

	@Override
	public Class<SpaceTimeCoordinateState> getAllowedType() {
		return SpaceTimeCoordinateState.class;
	}

}
