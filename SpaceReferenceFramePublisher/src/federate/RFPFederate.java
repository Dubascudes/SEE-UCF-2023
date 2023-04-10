package federate;

import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.AttributeNotOwned;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.ConnectionFailed;
import hla.rti1516e.exceptions.CouldNotCreateLogicalTimeFactory;
import hla.rti1516e.exceptions.CouldNotOpenFDD;
import hla.rti1516e.exceptions.ErrorReadingFDD;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.IllegalName;
import hla.rti1516e.exceptions.InconsistentFDD;
import hla.rti1516e.exceptions.InteractionClassNotDefined;
import hla.rti1516e.exceptions.InteractionClassNotPublished;
import hla.rti1516e.exceptions.InteractionParameterNotDefined;
import hla.rti1516e.exceptions.InvalidInteractionClassHandle;
import hla.rti1516e.exceptions.InvalidLocalSettingsDesignator;
import hla.rti1516e.exceptions.InvalidObjectClassHandle;
import hla.rti1516e.exceptions.NameNotFound;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.ObjectClassNotDefined;
import hla.rti1516e.exceptions.ObjectClassNotPublished;
import hla.rti1516e.exceptions.ObjectInstanceNameInUse;
import hla.rti1516e.exceptions.ObjectInstanceNameNotReserved;
import hla.rti1516e.exceptions.ObjectInstanceNotKnown;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;
import hla.rti1516e.exceptions.SynchronizationPointLabelNotAnnounced;
import hla.rti1516e.exceptions.UnsupportedCallbackModel;
import jat.core.util.PathUtil;
import jat.coreNOSA.ephemeris.DE405;
import jat.coreNOSA.ephemeris.DE405_Body;
import jat.coreNOSA.spacetime.Time;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Observable;
import java.util.Observer;
import java.util.TimeZone;

import jodd.datetime.JDateTime;
import jodd.datetime.JulianDateStamp;

import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.siso.spacefom.frame.ReferenceFrame;
import org.siso.spacefom.frame.ReferenceFrameManager;
import org.siso.spacefom.frame.time.FloatingPointTime;
import org.siso.spacefom.util.Matrix;
import org.siso.spacefom.util.QuaternionUtil;

import skf.model.interaction.modeTransitionRequest.ModeTransitionRequest;
import skf.model.object.annotations.ObjectClass;
import skf.model.object.executionConfiguration.ExecutionConfiguration;
import skf.model.object.executionConfiguration.ExecutionMode;
import skf.synchronizationPoint.SynchronizationPoint;
import planets.EarthMoonSystem;
import planets.Planet;
import planets.SolarSystemBarycenter;
import referenceFrame.ReferenceFrameObject;
import skf.config.Configuration;
import skf.core.SEEAbstractFederate;
import skf.exception.PublishException;
import skf.exception.SubscribeException;
import skf.exception.TimeOutException;
import skf.exception.UnsubscribeException;
import skf.exception.UpdateException;

public class RFPFederate extends SEEAbstractFederate implements Observer{

	private String LOCAL_SETTINGS_DESIGNATOR = null;

	private static final int MAX_WAIT_TIME = 100000; // milliseconds

	private ReferenceFrameManager RFmanager = null;
	private ReferenceFrameObject rfo_solarSystemBarycentricInertial = null;
	private ReferenceFrameObject rfo_sunCentricInertial = null;
	private ReferenceFrameObject rfo_sunCentricFixed = null;
	private ReferenceFrameObject rfo_earthMoonBarycentricInertial = null;
	private ReferenceFrameObject rfo_marsCentricInertial = null;
	private ReferenceFrameObject rfo_marsCentricFixed = null;
	private ReferenceFrameObject rfo_earthCentricInertial = null;
	private ReferenceFrameObject rfo_earthCentricFixed = null;
	private ReferenceFrameObject rfo_earthMoonBarycentricRotating = null;
	private ReferenceFrameObject rfo_moonCentricInertial = null;
	private ReferenceFrameObject rfo_earthMoonL2Rotating = null;
	private ReferenceFrameObject rfo_moonCentricFixed = null;
	private ReferenceFrameObject rfo_aitkenBasinFrame = null;
	private ReferenceFrameObject rfo_seeLspBaseFrame = null;

	private DE405 ephemeris = null;
	private Time sim_time = null;

	private Planet mars = null;
	private SolarSystemBarycenter solar_system_barycenter = null;
	private EarthMoonSystem earth_moon_system = null;

	private ModeTransitionRequest mtr = null;

	private SimpleDateFormat format = null;

	public RFPFederate(RFPAmbassador fedamb) throws RTIinternalError {
		super(fedamb);
		this.RFmanager = new ReferenceFrameManager();
		this.mtr = new ModeTransitionRequest();
		this.format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	private void initialize() {

		PathUtil ephem_path = new PathUtil();
		String dir_sep = ephem_path.fs;
		String DE405_data_dir = ephem_path.root_path + "data" + dir_sep + "core" + dir_sep + "ephemeris" + dir_sep + "DE405data" + dir_sep;

		System.out.println("Ephemeris file located in:");
		System.out.println("   " + DE405_data_dir);

		// Read in the Ephemeris data.
		this.ephemeris = new DE405(DE405_data_dir);

		// Initialize the reference frames.
		this.initialize_environment();

	}

	// Initialize the Sun-Earth-Moon environment.
	private void initialize_environment() {

		ReferenceFrame solarSystemBarycentricInertial = RFmanager.create(null, "SolarSystemBarycentricInertial");
		ReferenceFrame sunCentricInertial = RFmanager.create(solarSystemBarycentricInertial, "SunCentricInertial");
		ReferenceFrame sunCentricFixed = RFmanager.create(sunCentricInertial, "SunCentricFixed");
		ReferenceFrame earthMoonBarycentricInertial = RFmanager.create(solarSystemBarycentricInertial, "EarthMoonBarycentricInertial");
		ReferenceFrame marsCentricInertial = RFmanager.create(solarSystemBarycentricInertial, "MarsCentricInertial");
		ReferenceFrame marsCentricFixed = RFmanager.create(marsCentricInertial, "MarsCentricFixed");
		ReferenceFrame earthCentricInertial = RFmanager.create(earthMoonBarycentricInertial, "EarthCentricInertial");
		ReferenceFrame earthCentricFixed = RFmanager.create(earthCentricInertial, "EarthCentricFixed");
		ReferenceFrame earthMoonBarycentricRotating = RFmanager.create(earthMoonBarycentricInertial, "EarthMoonBarycentricRotating");
		ReferenceFrame moonCentricInertial = RFmanager.create(earthMoonBarycentricInertial, "MoonCentricInertial");
		ReferenceFrame earthMoonL2Rotating = RFmanager.create(earthMoonBarycentricInertial, "EarthMoonL2Rotating");
		ReferenceFrame moonCentricFixed = RFmanager.create(moonCentricInertial, "MoonCentricFixed");
		ReferenceFrame aitkenBasinFrame = RFmanager.create(moonCentricFixed, "AitkenBasinLocalFixed");
		ReferenceFrame seeLspBaseFrame = RFmanager.create(moonCentricFixed, "SeeLunarSouthPoleBaseLocalFixed");


		rfo_solarSystemBarycentricInertial = new ReferenceFrameObject(solarSystemBarycentricInertial);
		rfo_sunCentricInertial = new ReferenceFrameObject(sunCentricInertial);
		rfo_sunCentricFixed = new ReferenceFrameObject(sunCentricFixed);

		rfo_earthMoonBarycentricInertial = new ReferenceFrameObject(earthMoonBarycentricInertial);

		rfo_marsCentricInertial = new ReferenceFrameObject(marsCentricInertial);
		rfo_marsCentricFixed = new ReferenceFrameObject(marsCentricFixed);

		rfo_earthCentricInertial = new ReferenceFrameObject(earthCentricInertial);
		rfo_earthCentricFixed = new ReferenceFrameObject(earthCentricFixed);

		rfo_earthMoonBarycentricRotating = new ReferenceFrameObject(earthMoonBarycentricRotating);
		rfo_moonCentricInertial = new ReferenceFrameObject(moonCentricInertial);
		rfo_earthMoonL2Rotating = new ReferenceFrameObject(earthMoonL2Rotating);
		rfo_moonCentricFixed = new ReferenceFrameObject(moonCentricFixed);

		rfo_aitkenBasinFrame = new ReferenceFrameObject(aitkenBasinFrame);
		initializeAitkenBasinFrame();

		rfo_seeLspBaseFrame = new ReferenceFrameObject(seeLspBaseFrame);
		initializeSeeLspBaseFrame();

		// Instantiate the top level solar system barycenter frame.
		solar_system_barycenter = new SolarSystemBarycenter(rfo_solarSystemBarycentricInertial,
				rfo_sunCentricInertial, rfo_sunCentricFixed);

		// Instantiate the Earth-Moon system and associated frames.
		earth_moon_system = new EarthMoonSystem(rfo_earthMoonBarycentricInertial, rfo_earthMoonBarycentricRotating,
				rfo_earthCentricInertial, rfo_earthCentricFixed, rfo_moonCentricInertial, rfo_moonCentricFixed, rfo_earthMoonL2Rotating, sim_time);

		// Initialize the Earth-Moon system.
		earth_moon_system.initialize(sim_time, this.ephemeris);

		// Instantiate Mars and associated frames.
		mars = new Planet("Mars", rfo_marsCentricInertial, rfo_marsCentricFixed, DE405_Body.MARS );

		// Update the federation reference frames to the current epoch.
		solar_system_barycenter.update(sim_time, this.ephemeris);
		earth_moon_system.update(sim_time, this.ephemeris);
		mars.update(sim_time, this.ephemeris);

	}

	public void configure(Configuration config){
		super.configure(config);
		LOCAL_SETTINGS_DESIGNATOR = "crcHost="+config.getCrcHost()+"\ncrcPort="+config.getCrcPort();

		JulianDateStamp jds = new JulianDateStamp();
		jds.setTruncatedJulianDate(config.getSimulationScenarioTimeEphoc());

		JDateTime jdt = new JDateTime(jds);
		jdt.setTimeZone(TimeZone.getTimeZone("UTC"));

		this.sim_time = new Time(jdt.getJulianDate().getModifiedJulianDate().doubleValue());

		initialize();
	}

	@SuppressWarnings("unchecked")
	public void start() throws ConnectionFailed, InvalidLocalSettingsDesignator, UnsupportedCallbackModel, CallNotAllowedFromWithinCallback, RTIinternalError, CouldNotCreateLogicalTimeFactory, FederationExecutionDoesNotExist, InconsistentFDD, ErrorReadingFDD, CouldNotOpenFDD, SaveInProgress, RestoreInProgress, NotConnected, MalformedURLException, FederateNotExecutionMember, InterruptedException, InstantiationException, IllegalAccessException, NameNotFound, InvalidObjectClassHandle, AttributeNotDefined, ObjectClassNotDefined, SubscribeException, InvalidInteractionClassHandle, InteractionClassNotDefined, InteractionClassNotPublished, InteractionParameterNotDefined, PublishException, IllegalName, ObjectInstanceNameInUse, ObjectInstanceNameNotReserved, ObjectClassNotPublished, AttributeNotOwned, ObjectInstanceNotKnown, UpdateException {

		// --------------------- Join Federation Process ---------------------------- //
		// 1. Connect to RTI
		super.connectToRTI(LOCAL_SETTINGS_DESIGNATOR);

		// 2. Join Federation Execution
		super.joinFederationExecution();

		// 3. subscribeSubject, for internal SKF use.
		super.subscribeSubject(this);

		// 4. Waiting for the announcementof the SynchronizationPoint 'INITIALIZATION_STARTED'
		super.waitingForAnnouncement(SynchronizationPoint.INITIALIZATION_STARTED, MAX_WAIT_TIME);
		System.out.println("SynchronizationPoint.INITIALIZATION_STARTED has been announced!");

		// 5. Waiting for the announcement of the SynchronizationPoint 'OBJECTS_DISCOVERED'
		super.waitingForAnnouncement(SynchronizationPoint.OBJECTS_DISCOVERED, MAX_WAIT_TIME);
		System.out.println("SynchronizationPoint.OBJECTS_DISCOVERED has been announced!");

		// 6. Publish/Subscribe ObjectClasses and InteractionClasses
		super.subscribeElement((Class<? extends ObjectClass>) ExecutionConfiguration.class);
		super.publishInteraction(mtr);

		super.publishElement(rfo_solarSystemBarycentricInertial, rfo_solarSystemBarycentricInertial.getName());
		super.publishElement(rfo_sunCentricInertial, rfo_sunCentricInertial.getName());
		super.publishElement(rfo_earthMoonBarycentricInertial, rfo_earthMoonBarycentricInertial.getName());
		super.publishElement(rfo_earthMoonBarycentricRotating, rfo_earthMoonBarycentricRotating.getName());
		super.publishElement(rfo_earthCentricInertial, rfo_earthCentricInertial.getName());
		super.publishElement(rfo_earthCentricFixed, rfo_earthCentricFixed.getName());
		super.publishElement(rfo_moonCentricInertial, rfo_moonCentricInertial.getName());
		super.publishElement(rfo_moonCentricFixed, rfo_moonCentricFixed.getName());
		super.publishElement(rfo_earthMoonL2Rotating, rfo_earthMoonL2Rotating.getName());
		super.publishElement(rfo_marsCentricInertial, rfo_marsCentricInertial.getName());
		super.publishElement(rfo_marsCentricFixed, rfo_marsCentricFixed.getName());
		super.publishElement(rfo_aitkenBasinFrame, rfo_aitkenBasinFrame.getName());
		super.publishElement(rfo_seeLspBaseFrame, rfo_seeLspBaseFrame.getName());


		// 7. Achieve the SynchronizationPoint 'OBJECTS_DISCOVERED'
		try {
			super.achieveSynchronizationPoint(SynchronizationPoint.OBJECTS_DISCOVERED);
			System.out.println("SynchronizationPoint.OBJECTS_DISCOVERED has been achieved!");
		} catch (SynchronizationPointLabelNotAnnounced e) {
			e.printStackTrace();
		}

		// 8. Waiting for synchronization of the SynchronizationPoint 'OBJECTS_DISCOVERED'
		waitingForSynchronization(SynchronizationPoint.OBJECTS_DISCOVERED, MAX_WAIT_TIME);
		System.out.println("Federate synchronized with SynchronizationPoint.OBJECTS_DISCOVERED!");

		// 9. Request to the SpaceMAster federate the attribute update of the 'ExecutionConfiguration' ObjectClass
		long finishTime = System.currentTimeMillis() + MAX_WAIT_TIME;
		try {
			super.requestAttributeValueUpdate((Class<? extends ObjectClass>) ExecutionConfiguration.class);
		} catch (UnsubscribeException e) {
			e.printStackTrace();
		}

		// 10. Waiting for update of the 'ExecutionConfiguration' ObjectClass
		while(super.getExecutionConfiguration() == null ||
				getExecutionConfiguration().getScenario_time_epoch() == null){
			Thread.sleep(10);
			if(System.currentTimeMillis() > finishTime)
				throw new TimeOutException("time out exception");
		}

		// 11. Publish the Root ReferenceFrame object
		rfo_solarSystemBarycentricInertial.setName(null);
		super.updateElement(rfo_solarSystemBarycentricInertial);

		// 12. Wait for "ExCO" Update with Root Reference Frame Name
		finishTime = System.currentTimeMillis() + MAX_WAIT_TIME;
		while(super.getExecutionConfiguration().getRoot_frame_name() == null){
			Thread.sleep(10);
			if(System.currentTimeMillis() > finishTime)
				System.out.println("time out exception");
		}

		// 13. Achieve the SynchronizationPoint 'ROOT_FRAME_DISCOVERED'
		try {
			super.achieveSynchronizationPoint(SynchronizationPoint.ROOT_FRAME_DISCOVERED);
			System.out.println("SynchronizationPoint.ROOT_FRAME_DISCOVERED has been achieved!");
		} catch (SynchronizationPointLabelNotAnnounced e) {
			e.printStackTrace();
		}
		waitingForSynchronization(SynchronizationPoint.ROOT_FRAME_DISCOVERED, MAX_WAIT_TIME);
		System.out.println("Federate synchronized with SynchronizationPoint.ROOT_FRAME_DISCOVERED!");

		/* 14. Start Early Joiner Multiphase initiaization*/
		boolean multiphase = false;
		try {
			super.achieveSynchronizationPoint(SynchronizationPoint.MPI1);
			System.out.println("SynchronizationPoint.MPI1 has been achieved!");
			multiphase = true;
		} catch (SynchronizationPointLabelNotAnnounced e) {
			//ignored
		}

		if(multiphase){
			waitingForSynchronization(SynchronizationPoint.MPI1, MAX_WAIT_TIME);
			System.out.println("Federate synchronized with SynchronizationPoint.MPI1!");

			try {
				super.achieveSynchronizationPoint(SynchronizationPoint.MPI2);
				System.out.println("SynchronizationPoint.MPI2 has been achieved!");
			} catch (SynchronizationPointLabelNotAnnounced e) {
				e.printStackTrace();
			}
			waitingForSynchronization(SynchronizationPoint.MPI2, MAX_WAIT_TIME);
			System.out.println("Federate synchronized with SynchronizationPoint.MPI2!");
		}
		/*End Early Joiner Multiphase initiaization*/

		// 15. setup time management
		super.setupHLATimeManagement();

		try {
			super.achieveSynchronizationPoint(SynchronizationPoint.INITIALIZATION_STARTED);
			System.out.println("SynchronizationPoint.INITIALIZATION_STARTED has been achieved!");
		} catch (SynchronizationPointLabelNotAnnounced e) {
			e.printStackTrace();
		}

		// 16.  Waiting for synchronization of the 'INITIALIZATION_STARTED'
		waitingForSynchronization(SynchronizationPoint.INITIALIZATION_STARTED, MAX_WAIT_TIME);
		System.out.println("Federate synchronized with SynchronizationPoint.INITIALIZATION_STARTED!");

		// 17. setup time management
		super.startExecution();

	}
	/*
	 * Master Run - Process and Run Jobs
	 * The TAG - TAR cycle is provided by the DKF
	 */
	@Override
	protected void doAction() {

		sim_time.update(super.getTime().getFederationTimeCycle()/1000000.0d);
		solar_system_barycenter.update(sim_time, this.ephemeris);
		earth_moon_system.update(sim_time, this.ephemeris);
		mars.update(sim_time, this.ephemeris);

		double time = rfo_earthMoonBarycentricInertial.getState().getTime().getValue().doubleValue();
		((FloatingPointTime)rfo_solarSystemBarycentricInertial.getState().getTime()).setValue(time);
		((FloatingPointTime)rfo_aitkenBasinFrame.getState().getTime()).setValue(time);
		((FloatingPointTime)rfo_seeLspBaseFrame.getState().getTime()).setValue(time);

		try {
			super.updateElement(rfo_solarSystemBarycentricInertial);
			super.updateElement(rfo_sunCentricInertial);
			super.updateElement(rfo_earthMoonBarycentricInertial);
			super.updateElement(rfo_earthMoonBarycentricRotating);
			super.updateElement(rfo_earthCentricInertial);
			super.updateElement(rfo_earthCentricFixed);
			super.updateElement(rfo_moonCentricInertial);
			super.updateElement(rfo_moonCentricFixed);
			super.updateElement(rfo_earthMoonL2Rotating);
			super.updateElement(rfo_marsCentricInertial);
			super.updateElement(rfo_marsCentricFixed);
			super.updateElement(rfo_aitkenBasinFrame);
			super.updateElement(rfo_seeLspBaseFrame);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void initializeAitkenBasinFrame() {
		// Set the location of the Aitken Basin frame from MoonCetericFixed.
		rfo_aitkenBasinFrame.getState().getTranslationalState().setPosition(new Vector3D(-817582.939286128, -296194.936333636, -1504977.52696795));
		rfo_aitkenBasinFrame.getState().getTranslationalState().setVelocity(Vector3D.ZERO);

		// Set the attitude of the Aitken Basin frame from MoonCetericFixed.
		rfo_aitkenBasinFrame.getState().getRotationState().setAttitudeQuaternion(new Quaternion(-0.212035899134992,
				new double[]{-0.79079044533773, 0.554597207736449, 0.148705030888344}));

		// Compute the corresponding attitude transformation.
		Matrix matrix = QuaternionUtil.quaternionToMatrix(rfo_aitkenBasinFrame.getState().getRotationState().getAttitudeQuaternion());
		rfo_aitkenBasinFrame.setT_parent_body(matrix);
	}

	private void initializeSeeLspBaseFrame() {
		// Set the location of the SEE LSP Base (NED) frame from MoonCetericFixed.
		rfo_seeLspBaseFrame.getState().getTranslationalState().setPosition(new Vector3D(-10904.95664, -7550.94997, -1739036.92673));
		rfo_seeLspBaseFrame.getState().getTranslationalState().setVelocity(Vector3D.ZERO);

		// Set the attitude of the SEE LSP Base (NED) frame from MoonCetericFixed.
		rfo_seeLspBaseFrame.getState().getRotationState().setAttitudeQuaternion(new Quaternion(0.2982057783860692,
				new double[]{0.003640022857714461, 0.001137226493544214, 0.9544939867210565}));

		// Compute the corresponding attitude transformation.
		Matrix matrix = QuaternionUtil.quaternionToMatrix(rfo_seeLspBaseFrame.getState().getRotationState().getAttitudeQuaternion());
		rfo_seeLspBaseFrame.setT_parent_body(matrix);
	}

	@Override
	public void update(Observable o, Object arg) {
		if(arg instanceof ExecutionConfiguration){
			super.setExecutionConfiguration((ExecutionConfiguration)arg);
			System.out.println("**Recived** " + super.getExecutionConfiguration());

			if(super.getExecutionConfiguration().getCurrent_execution_mode() == ExecutionMode.EXEC_MODE_RUNNING &&
					super.getExecutionConfiguration().getNext_execution_mode() == ExecutionMode.EXEC_MODE_FREEZE){
				super.freezeExecution();
			}

			else if(super.getExecutionConfiguration().getCurrent_execution_mode() == ExecutionMode.EXEC_MODE_FREEZE &&
					super.getExecutionConfiguration().getNext_execution_mode() == ExecutionMode.EXEC_MODE_RUNNING){
				super.resumeExecution();
			}

			else if((super.getExecutionConfiguration().getCurrent_execution_mode() == ExecutionMode.EXEC_MODE_FREEZE ||
					super.getExecutionConfiguration().getCurrent_execution_mode() == ExecutionMode.EXEC_MODE_RUNNING ) &&
					super.getExecutionConfiguration().getNext_execution_mode() == ExecutionMode.EXEC_MODE_SHUTDOWN ){
				super.shudownExecution();
			}
		}
		else {
			System.out.println("**Recived2** " + arg);
		}
	}

	public ModeTransitionRequest getMTR() {
		return this.mtr;
	}

}
