/**
 * Moon environment model.
 *
 * Provides the Moon environment model based on the Java Astrodynamics
 * Toolkit (JAT).
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

// JAT Moon model.
import jat.coreNOSA.spacetime.Time;
import jat.coreNOSA.ephemeris.DE405;
import jat.coreNOSA.ephemeris.DE405_Body;

public class Moon extends Planet {

	public Moon(ReferenceFrameObject moonCentricInertial, ReferenceFrameObject moonCentricFixed) {
		super("Moon", moonCentricInertial, moonCentricFixed, DE405_Body.MOON);
	}

	public void update(ReferenceFrameObject earthMoon_barycentric_frame, Time ephem_time, DE405 ephemeris) {

		Vector3D moon_pos; // Position vector of Moon.
		Vector3D moon_vel; // Velocity vector of Moon.
		Vector3D m_lib_pos;   // Moon libration angles.
		Vector3D m_lib_vel;   // Moon libration angles.

		// Get the JAT Moon position and velocity.
		// NOTE: This will be in kilometers in the Solar System Barycenter frame.
		// NOTE: LCI for Smackdown is in meters in the EM Barycentric frame.
		moon_pos = new Vector3D(ephemeris.get_planet_posvel(DE405_Body.MOON, ephem_time).get(0, 3).x);
		moon_vel = new Vector3D(ephemeris.get_planet_posvel(DE405_Body.MOON, ephem_time).get(3, 3).x);

		// Compute the Moon position and velocity wrt EM Barycenter.
		inertial.getState().getTranslationalState().setPosition(moon_pos.scalarMultiply(KM2M).
				subtract(earthMoon_barycentric_frame.getState().getTranslationalState().getPosition()));
		
		inertial.getState().getTranslationalState().setVelocity(moon_vel.scalarMultiply(KM2M).
				subtract(earthMoon_barycentric_frame.getState().getTranslationalState().getVelocity()));

		// By definition, inertial frames are aligned.
		inertial.getT_parent_body().identity();

		inertial.getState().getRotationState().setAttitudeQuaternion(Quaternion.IDENTITY);
		inertial.getState().getRotationState().setAngularVelocityVector(Vector3D.ZERO);

		// Set the time.
		((FloatingPointTime)inertial.getState().getTime()).setValue(ephem_time.mjd_tt()-MJD_TJD_OFFSET);

		// Compute the Moon's planet fixed position.
		// NOTE: This is always zero since this frame is colocated with LCI.
		fixed.getState().getTranslationalState().setPosition(Vector3D.ZERO);
		fixed.getState().getTranslationalState().setVelocity(Vector3D.ZERO);

		// Get the lunar libration angles and rates from the ephemeris.
		m_lib_pos = new Vector3D(ephemeris.get_planet_posvel(DE405_Body.MOON_LIB, ephem_time).get(0, 3).x);
		m_lib_vel = new Vector3D(ephemeris.get_planet_posvel(DE405_Body.MOON_LIB, ephem_time).get(3, 3).x);

		// Declare some working variables.
		double phi      = m_lib_pos.getX();
		double theta    = m_lib_pos.getY();
		double psi      = m_lib_pos.getZ();
		double phidot   = m_lib_vel.getX();
		double thetadot = m_lib_vel.getY();
		double psidot   = m_lib_vel.getZ();

		// Declare and compute select trigonometric values.
		double cosphi   = Math.cos(phi);
		double sinphi   = Math.sin(phi);
		double costheta = Math.cos(theta);
		double sintheta = Math.sin(theta);
		double cospsi   = Math.cos(psi);
		double sinpsi   = Math.sin(psi);

		// Compute the Moon's planet fixed attitude.

		// First the transformation matirx from intertial to body.
		Matrix fixedTParentBody = fixed.getT_parent_body();
		fixedTParentBody.setValue(0, 0, cospsi*cosphi - sinpsi*costheta*sinphi);
		fixedTParentBody.setValue(0, 1, cospsi*sinphi + sinpsi*costheta*cosphi);
		fixedTParentBody.setValue(0, 2, sinpsi*sintheta);

		fixedTParentBody.setValue(1, 0, -sinpsi*cosphi - cospsi*costheta*sinphi);
		fixedTParentBody.setValue(1, 1, -sinpsi*sinphi + cospsi*costheta*cosphi);
		fixedTParentBody.setValue(1, 2,  cospsi*sintheta);

		fixedTParentBody.setValue(2, 0,  sintheta*sinphi);
		fixedTParentBody.setValue(2, 1, -sintheta*cosphi);
		fixedTParentBody.setValue(2, 2,  costheta);

		// Then compute the corresponding attitude quaternion.
		fixed.getState().getRotationState().setAttitudeQuaternion(QuaternionUtil.matrixToQuaternion(fixed.getT_parent_body()));

		// Compute the Moon's rotation vector.
		fixed.getState().getRotationState().setAngularVelocityVector(
				new Vector3D(
						phidot*sintheta*sinpsi + thetadot*cospsi,
						phidot*sintheta*cospsi - thetadot*sinpsi,
						phidot*costheta + psidot
						));

		// Set the time.
		((FloatingPointTime)fixed.getState().getTime()).setValue(ephem_time.mjd_tt() - MJD_TJD_OFFSET);

	}


}
