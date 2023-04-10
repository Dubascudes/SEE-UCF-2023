/**
 * Solar system barycentric environment model.
 *
 * Provides the basic enviroment for the Sun and Solar system barycenter.
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

// Smackdown utility classes.
import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.siso.spacefom.frame.time.FloatingPointTime;

import referenceFrame.ReferenceFrameObject;

// JAT Earth model.
import jat.coreNOSA.spacetime.Time;
import jat.coreNOSA.ephemeris.DE405;
import jat.coreNOSA.ephemeris.DE405_Body;

public class SolarSystemBarycenter {

   // Declare the sun and the solar system barycenter reference frame data.
   public Planet sun = null;
   public String name = null;
   public ReferenceFrameObject inertial = null;

   // SolarSystemBarycenterary Ephemeris body designator,
   private DE405_Body planet_id = null;

   public SolarSystemBarycenter(ReferenceFrameObject solarSystemBarycentricInertial, ReferenceFrameObject sunCentricInertial,
		   ReferenceFrameObject sunCentricFixed) {
	   
      this.name = solarSystemBarycentricInertial.getName();
      this.planet_id = DE405_Body.SOLAR_SYSTEM_BARY;
      this.inertial = solarSystemBarycentricInertial;
      this.sun = new Planet("Sun", sunCentricInertial, sunCentricFixed, DE405_Body.SUN);
   }


   public void update(Time ephem_time, DE405 ephemeris) {
	   
	  Vector3D sun_pos; // Position and velocity vector of Sun.
	  Vector3D sun_vel; // Velocity vector of Sun.
	   
	  sun_pos = new Vector3D(ephemeris.get_planet_posvel(planet_id, ephem_time).get(0,3).x);
	  sun_vel = new Vector3D(ephemeris.get_planet_posvel(planet_id, ephem_time).get(3,3).x);

      // For now, this should be zero.  We may want to differentiate from
      // the sun inertial frame and the solar system barycenter frame in the
      // near future.
      inertial.getState().getTranslationalState().setPosition(sun_pos.scalarMultiply(Planet.KM2M));
      inertial.getState().getTranslationalState().setVelocity(sun_vel.scalarMultiply(Planet.KM2M));

      // By definition, inertial frames are aligned.
      inertial.getState().getRotationState().setAttitudeQuaternion(Quaternion.IDENTITY);
      inertial.getT_parent_body().identity();
      
      inertial.getState().getRotationState().setAngularVelocityVector(Vector3D.ZERO);

      // Set the time.
      ((FloatingPointTime)inertial.getState().getTime()).setValue(ephem_time.mjd_tt() - Planet.MJD_TJD_OFFSET);

      // Update the Sun.
      sun.update(ephem_time, ephemeris);

   }

}
