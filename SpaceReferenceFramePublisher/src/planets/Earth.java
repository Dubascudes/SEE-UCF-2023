/**
 * Earth environment model.
 *
 * Provides the Earth environment model based on the Java Astrodynamics
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

import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.siso.spacefom.frame.time.FloatingPointTime;
import org.siso.spacefom.util.Matrix;
import org.siso.spacefom.util.QuaternionUtil;

import referenceFrame.ReferenceFrameObject;
import jat.coreNOSA.spacetime.Time;
import jat.coreNOSA.ephemeris.DE405;
import jat.coreNOSA.ephemeris.DE405_Body;
import jat.coreNOSA.spacetime.EarthRef;

public class Earth extends Planet {

	private EarthRef earth_ref;

	public Earth(ReferenceFrameObject earthCentricInertial, ReferenceFrameObject earthCentricFixed, Time time) {
		super( "Earth", earthCentricInertial, earthCentricFixed, DE405_Body.EARTH );
		earth_ref = new EarthRef(time);
	}

	public void update(ReferenceFrameObject earthMoon_barycentric_frame, Time ephem_time, DE405  ephemeris) {

		Vector3D earth_pos; // Position vector of Earth.
		Vector3D earth_vel; // Velocity vector of Earth.

		// Update the JAT Earth reference.
		this.earth_ref.update( ephem_time );

		// Get the JAT Earth position and velocity.
		// NOTE: This will be in kilometers in the Solar System Barycenter frame.
		// NOTE: ECI for Smackdown is in meters in the EM Barycentric frame.
		earth_pos = new Vector3D(ephemeris.get_planet_posvel(DE405_Body.EARTH, ephem_time).get(0, 3).x);
		earth_vel = new Vector3D(ephemeris.get_planet_posvel(DE405_Body.EARTH, ephem_time).get(3, 3).x);
		
		// Compute the Earth position and velocity wrt EM Barycenter.
		inertial.getState().getTranslationalState().setPosition(earth_pos.scalarMultiply(KM2M).subtract(earthMoon_barycentric_frame.getState().getTranslationalState().getPosition()));
		inertial.getState().getTranslationalState().setVelocity(earth_vel.scalarMultiply(KM2M).subtract(earthMoon_barycentric_frame.getState().getTranslationalState().getVelocity()));
		
		// By definition, inertial frames are aligned.
		inertial.getT_parent_body().identity();;
		
		// Rotational State
		inertial.getState().getRotationState().setAttitudeQuaternion(Quaternion.IDENTITY);
		inertial.getState().getRotationState().setAngularVelocityVector(Vector3D.ZERO);

		// Set the time.
		((FloatingPointTime)inertial.getState().getTime()).setValue(ephem_time.mjd_tt()-MJD_TJD_OFFSET);

		// Compute the Earth's planet fixed position.
		// NOTE: This is always zero since this frame is colocated with ECI.
		fixed.getState().getTranslationalState().setPosition(Vector3D.ZERO);
		// NOTE: Velocity relative to ECI is always zero.
		fixed.getState().getTranslationalState().setVelocity(Vector3D.ZERO);

		// Compute the Earth's planet fixed attitude.
		Matrix fixedT_parent_body = fixed.getT_parent_body();
		for(int i = 0; i < fixedT_parent_body.getNumberOfRows(); i++)
			for(int j = 0; j < fixedT_parent_body.getNumberOfColumns(); j++)
				fixedT_parent_body.setValue(i, j, earth_ref.ECI2ECEF().A[i][j]);
		

		fixed.getState().getRotationState().setAttitudeQuaternion(QuaternionUtil.matrixToQuaternion(fixedT_parent_body));

		// Compute the Earth's rotation vector.
		fixed.getState().getRotationState().setAngularVelocityVector(new Vector3D(0.0, 0.0, EarthRef.omega_e));

		// Set the time.
		((FloatingPointTime)fixed.getState().getTime()).setValue(ephem_time.mjd_tt()-MJD_TJD_OFFSET);

	}
}
