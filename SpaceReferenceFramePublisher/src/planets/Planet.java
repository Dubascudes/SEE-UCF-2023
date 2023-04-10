/**
 * General planet environment model.
 *
 * Provides the generic planet environment model.
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

import referenceFrame.ReferenceFrameObject;

// JAT Earth model.
import jat.coreNOSA.spacetime.Time;
import jat.coreNOSA.ephemeris.DE405;
import jat.coreNOSA.ephemeris.DE405_Body;

public class Planet {

	static final long KM2M = 1000; // Conversion from kilometers to meters.
	static final long MJD_TJD_OFFSET = 40000; // Offset between mjd and tdt.

	// Declare the planet reference frame data.
	public String name = null;
	public ReferenceFrameObject inertial = null;
	public ReferenceFrameObject fixed = null;

	// Planetary Ephemeris body designator,
	private DE405_Body planet_id = null;

	public Planet (String planet_name, ReferenceFrameObject inertial, ReferenceFrameObject fixed, DE405_Body planet_id ) {
		
		this.name = planet_name;
		this.planet_id = planet_id;
		this.inertial = inertial;
		this.fixed = fixed;
	}

	public void update(Time ephem_time, DE405 ephemeris) {

		Vector3D earth_pos; // Position and velocity vector of Earth.
		Vector3D earth_vel; // Velocity vector of Earth.
		
		// Get the JAT planet position and velocity.
		// NOTE: This will be in kilometers in the Solar System Barycenter frame.
		earth_pos = new Vector3D(ephemeris.get_planet_posvel(planet_id, ephem_time).get(0, 3).x);
		earth_vel = new Vector3D(ephemeris.get_planet_posvel(planet_id, ephem_time).get(3, 3).x);

		// Compute the planet's position and velocity.
		inertial.getState().getTranslationalState().setPosition(earth_pos.scalarMultiply(KM2M));
		inertial.getState().getTranslationalState().setVelocity(earth_vel.scalarMultiply(KM2M));

		// By definition, inertial frames are aligned.
		inertial.getT_parent_body().identity();

		inertial.getState().getRotationState().setAttitudeQuaternion(Quaternion.IDENTITY);
		inertial.getState().getRotationState().setAngularVelocityVector(Vector3D.ZERO);

		// Set the time.
		((FloatingPointTime) inertial.getState().getTime()).setValue(ephem_time.mjd_tt()-MJD_TJD_OFFSET);

		// Unfortunately, only the planetary attitude of the Earth and the Moon
		// are available in the JAT DE405 implementation.  That's not unusual in
		// that most attitude models are very planet specific and not really
		// well know.
		// So, the planetary attitude is NOT updated here yet.

		// Set the time.
		((FloatingPointTime) fixed.getState().getTime()).setValue(ephem_time.mjd_tt()-MJD_TJD_OFFSET);
	}
}
