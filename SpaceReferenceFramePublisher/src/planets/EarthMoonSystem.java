/**
 * Earth-Moon system environment model.
 *
 * Provides an environment model for the two-body Earth-Moon system.  This
 * code uses the Java Astrodynamics Toolkit (JAT).
 *
 * @author   Edwin Z. Crues <edwin.z.crues@nasa.gov>
 * @version  0.0
 */
/**
 * Copyright 2015 Edwin Z. Crues
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package planets;

// JAT utility classes.
import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.siso.spacefom.frame.time.FloatingPointTime;
import org.siso.spacefom.util.Matrix;
import org.siso.spacefom.util.QuaternionUtil;

import referenceFrame.ReferenceFrameObject;

// JAT models.
import jat.coreNOSA.constants.*;
import jat.coreNOSA.spacetime.Time;
import jat.coreNOSA.ephemeris.DE405;
import jat.coreNOSA.ephemeris.DE405_Body;

public class EarthMoonSystem {

	public class Barycenter {

		ReferenceFrameObject inertial;
		ReferenceFrameObject rotating;

		public Barycenter (ReferenceFrameObject inertial, ReferenceFrameObject rotating) {
			this.inertial = inertial;
			this.rotating = rotating;
		}
	}

	// Define the Earth, Moon and Earth-Moon Barycenter frame.
	public Earth      earth;
	public Moon       moon;
	public Barycenter barycenter;

	// Define the L2 Lagrange point reference frame.
	public ReferenceFrameObject l2_frame;

	// Lagrange point working variables.
	private double  body_mass_ratio;
	private double  system_mu_ratio;
	private double  body2_to_L2_scale_factor;

	// UTC time standard constructor.
	public EarthMoonSystem(ReferenceFrameObject earthMoonBarycentricInertial, ReferenceFrameObject earthMoonBarycentricRotating,
			ReferenceFrameObject earthCentricInertial, ReferenceFrameObject earthCentricFixed, ReferenceFrameObject moonCentricInertial, 
			ReferenceFrameObject moonCentricFixed, ReferenceFrameObject earthMoonL2Rotating, Time time) {
		
		// Instantiate the Earth-Moon barycentric reference frames.
		this.barycenter = new Barycenter(earthMoonBarycentricInertial, earthMoonBarycentricRotating);

		// Instantiate an Earth.
		this.earth = new Earth(earthCentricInertial, earthCentricFixed, time);

		// Instantiate a Moon.
		this.moon = new Moon(moonCentricInertial, moonCentricFixed);

		// Instantiate an Earth-Moon L2 reference frame.
		//this.l2_frame = new ReferenceFrame( "EarthMoonBarycentricInertial",
		//                                  "EarthMoonL2Rotating"          );
		this.l2_frame = earthMoonL2Rotating;

	}

	public void initialize(Time ephem_time, DE405 ephemeris) {

		// First we update the Earth-Moon barycentric inertial frame.
		this.update_barycenter_inertial(ephem_time, ephemeris);

		// Next we update the Earth reference frames.
		this.earth.update(this.barycenter.inertial, ephem_time, ephemeris);

		// Next, we update the Moon reference frames.
		this.moon.update(this.barycenter.inertial, ephem_time, ephemeris);

		// First we update the Earth-Moon barycentric rotating frame.
		this.update_barycenter_rotating(ephem_time);

		// Next, we initialize the L2 frame.
		this.initialize_L2_frame();

		// Finally, we update the L2 reference frame.
		this.update_L2_frame(ephem_time);
	}


	private void initialize_L2_frame() {

		// Set up body mass ratio and mu ratios of the system; these are constant
		// parameters that will enable calculation of position and velocity of
		// the managed frame with respect to the system barycenter (note that the
		// system barycenter is the direct parent to this frame)
		body_mass_ratio = IERS_1996.Moon_Earth_Ratio;
		system_mu_ratio = 1.0 + body_mass_ratio;

		// Set up the ratio of (L2 dist from body 1) / (body 2 dist from body 1);
		// requires solution of the fifth-order equation
		//   (1+a)*x^5 + (3+2a)*x^4 + (3+a)*x^3 -a*x^2 - 2a*x - a = 0
		// where alpha is the body mass ratio (as defined above) and x is the
		// desired distance ratio. Solve for the real solution for x via
		// Newton-Raphson. 
		double alpha = body_mass_ratio;
		double x5_coeff = 1.0 + alpha;
		double x4_coeff = 3.0 + 2.0*alpha;
		double x3_coeff = 3.0 + alpha;
		double x2_coeff = alpha;
		double x1_coeff = 2.0*alpha;
		double x0_coeff = alpha;

		double x_real = 0.2;
		double d_x = 1.0;
		double func;
		double d_func;

		while ((d_x > 1e-15) || (d_x < -1e-15)) {
			func  = x5_coeff*Math.pow(x_real,5.0) + x4_coeff*Math.pow(x_real,4.0)
					+ x3_coeff*Math.pow(x_real,3.0) - x2_coeff*Math.pow(x_real,2.0)
					- x1_coeff*x_real          - x0_coeff;

			d_func = 5.0*x5_coeff*Math.pow(x_real,4.0) + 4.0*x4_coeff*Math.pow(x_real,3.0)
					+ 3.0*x3_coeff*Math.pow(x_real,2.0) - 2.0*x2_coeff*x_real
					-x1_coeff;

			d_x = -func / d_func;
			x_real += d_x;
		}

		// Build final overall constant to be used to scale position and velocity
		// of body 2 (wrt system barycenter) to the pos/vel of the L2 point (also
		// with respect to the system barycenter).
		body2_to_L2_scale_factor = 1.0 + (x_real * system_mu_ratio);


		// Extract the barycenter-to-body2 position and velocity vectors; NOTE
		// this assumes that the barycenter inertial frame is the parent frame
		// to body 2, which should be valid for all 3-body systems
		// Set initial pos/vel of the center of the L2 frame wrt the barycenter
		Vector3D vector = moon.inertial.getState().getTranslationalState().getPosition().scalarMultiply(body2_to_L2_scale_factor);
		l2_frame.getState().getTranslationalState().setPosition(vector);

		vector = moon.inertial.getState().getTranslationalState().getVelocity().scalarMultiply(body2_to_L2_scale_factor);
		l2_frame.getState().getTranslationalState().setVelocity(vector);

	}


	public void update(Time ephem_time, DE405 ephemeris) {

		// First we update the Earth-Moon barycentric inertial frame.
		this.update_barycenter_inertial( ephem_time, ephemeris );

		// Next we update the Earth reference frames.
		this.earth.update( this.barycenter.inertial, ephem_time, ephemeris );

		// Next, we update the Moon reference frames.
		this.moon.update( this.barycenter.inertial, ephem_time, ephemeris );

		// First we update the Earth-Moon barycentric rotating frame.
		this.update_barycenter_rotating( ephem_time );

		// Finally, we update the L2 reference frame.
		this.update_L2_frame( ephem_time );

	}


	private void update_barycenter_inertial(Time  ephem_time, DE405 ephemeris) {
		
		Vector3D em_pos; // Position vector of EM baricenter.
		Vector3D em_vel; // Velocity vector of EM baricenter.

		// Get the JAT Earth-Moon barycenter position and velocity.
		// NOTE: This will be in kilometers in the Solar System Barycenter frame.
		em_pos = new Vector3D(ephemeris.get_planet_posvel(DE405_Body.EM_BARY, ephem_time).get(0, 3).x);
		em_vel = new Vector3D(ephemeris.get_planet_posvel(DE405_Body.EM_BARY, ephem_time).get(3, 3).x);

		//
		// Update the barycenter inertial frame.
		//
		// Get position and velocity meters from ephemeris.
		barycenter.inertial.getState().getTranslationalState().setPosition(em_pos.scalarMultiply(Planet.KM2M));
		barycenter.inertial.getState().getTranslationalState().setVelocity(em_vel.scalarMultiply(Planet.KM2M));

		// By definition, inertial frames are aligned.
		barycenter.inertial.getState().getRotationState().setAttitudeQuaternion(Quaternion.IDENTITY);
		barycenter.inertial.getT_parent_body().identity();
		barycenter.inertial.getState().getRotationState().setAngularVelocityVector(Vector3D.ZERO);

		// Set the time.
		((FloatingPointTime)this.barycenter.inertial.getState().getTime()).setValue(ephem_time.mjd_tt()-Planet.MJD_TJD_OFFSET);

	}

	private void update_barycenter_rotating(Time  ephem_time) {
		/*
	 	* Update the barycenter rotating frame.
		* Bt definition, the roating frame is centered on the inertial frame.
		*/
		barycenter.rotating.getState().getTranslationalState().setPosition(Vector3D.ZERO);
		barycenter.rotating.getState().getTranslationalState().setVelocity(Vector3D.ZERO);

		// Compute the attiude information for the rotating frame.
		compute_rotating_frame(moon.inertial, barycenter.rotating);

		// Set the time.
		((FloatingPointTime)this.barycenter.rotating.getState().getTime()).setValue(ephem_time.mjd_tt()-Planet.MJD_TJD_OFFSET);
	}

	private void  update_L2_frame(Time ephem_time) {

		/*
		* Compute position and velocity of the L2 frame wrt the barycenter.
		* NOTE: This assumes that the barycenter inertial frame is the parent
		* frame, which should be valid for all 3-body systems.
		*/ 
		Vector3D pos_vector = moon.inertial.getState().getTranslationalState().getPosition().scalarMultiply(body2_to_L2_scale_factor);
		l2_frame.getState().getTranslationalState().setPosition(pos_vector);
		
		Vector3D vel_vector = moon.inertial.getState().getTranslationalState().getVelocity().scalarMultiply(body2_to_L2_scale_factor);
		l2_frame.getState().getTranslationalState().setVelocity(vel_vector);
		
		/*
		* By definition, the L2 frame attitude and attitude rate are the same
		* as the Earth-Moon barycenter frame.
		*/
		l2_frame.getState().getRotationState().setAttitudeQuaternion(
				new Quaternion(
				barycenter.rotating.getState().getRotationState().getAttitudeQuaternion().getQ0(),
				barycenter.rotating.getState().getRotationState().getAttitudeQuaternion().getQ1(),
				barycenter.rotating.getState().getRotationState().getAttitudeQuaternion().getQ2(),
				barycenter.rotating.getState().getRotationState().getAttitudeQuaternion().getQ3()
				));
		
		Matrix bary_matrix = barycenter.rotating.getT_parent_body();
		for(int i = 0; i < bary_matrix.getNumberOfRows(); i++)
			for(int j = 0; j < bary_matrix.getNumberOfColumns(); j++)
				l2_frame.getT_parent_body().setValue(i, j, bary_matrix.getValue(i, j));
		
		l2_frame.getState().getRotationState().setAngularVelocityVector(
				new Vector3D(
						barycenter.rotating.getState().getRotationState().getAngularVelocityVector().getX(),
						barycenter.rotating.getState().getRotationState().getAngularVelocityVector().getY(),
						barycenter.rotating.getState().getRotationState().getAngularVelocityVector().getZ()
				));

		// Set the time.
		((FloatingPointTime)l2_frame.getState().getTime()).setValue(ephem_time.mjd_tt() - Planet.MJD_TJD_OFFSET);

	}


	private void  compute_rotating_frame(ReferenceFrameObject body_frame, ReferenceFrameObject rotating_frame) {
		// Declare the working variables.
		double    posn_mag_sq;
		Vector3D posn_unit_vector = null;
		Vector3D vel_unit_vector = null;
		Vector3D momentum_vector = null;
		Vector3D momentum_unit_vector = null;
		Vector3D y_unit_axis = null;
		Vector3D ang_rate_in_inertial = null;

		// Compute the position and velocity unit vectors.
		posn_unit_vector = new Vector3D(
				body_frame.getState().getTranslationalState().getPosition().getX(),
				body_frame.getState().getTranslationalState().getPosition().getY(),
				body_frame.getState().getTranslationalState().getPosition().getZ());
		posn_unit_vector = posn_unit_vector.normalize();
		
		vel_unit_vector = new Vector3D(
				body_frame.getState().getTranslationalState().getVelocity().getX(),
				body_frame.getState().getTranslationalState().getVelocity().getY(),
				body_frame.getState().getTranslationalState().getVelocity().getZ());
		vel_unit_vector = vel_unit_vector.normalize();

		// Compute the angular momentum vector and momentum unit vector.
		momentum_vector = body_frame.getState().getTranslationalState().getPosition().crossProduct(
				body_frame.getState().getTranslationalState().getVelocity());
		
		momentum_unit_vector = new Vector3D(
				momentum_vector.getX(),
				momentum_vector.getY(),
				momentum_vector.getZ()
				);
		momentum_unit_vector = momentum_unit_vector.normalize();

		// Compute the Y-axis direction.
		y_unit_axis = momentum_unit_vector.crossProduct(posn_unit_vector);
		y_unit_axis = y_unit_axis.normalize();

		// Build the transformation matrix.
		for (int i = 0; i < 3; i++) {
			rotating_frame.getT_parent_body().setValue(0, i, posn_unit_vector.toArray()[i]);
			rotating_frame.getT_parent_body().setValue(1, i, y_unit_axis.toArray()[i]);
			rotating_frame.getT_parent_body().setValue(2, i, momentum_unit_vector.toArray()[i]);
		}

		// Compute the corresponding quaternion.
		rotating_frame.getState().getRotationState().setAttitudeQuaternion(
				QuaternionUtil.matrixToQuaternion(rotating_frame.getT_parent_body()));

		// Compute the EM L2 rotation vector.
		posn_mag_sq = body_frame.getState().getTranslationalState().getPosition().getNormSq();
		ang_rate_in_inertial = momentum_vector.scalarMultiply(1.0/posn_mag_sq);
		
		Vector3D vector = new Vector3D(rotating_frame.getT_parent_body().multiply(ang_rate_in_inertial.toArray()));
		rotating_frame.getState().getRotationState().setAngularVelocityVector(vector);
		
	}
}
