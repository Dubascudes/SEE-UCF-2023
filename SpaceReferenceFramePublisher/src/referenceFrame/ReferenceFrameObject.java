package referenceFrame;

import org.siso.spacefom.frame.ReferenceFrame;
import org.siso.spacefom.frame.SpaceTimeCoordinateState;
import org.siso.spacefom.frame.time.Ephoc;
import org.siso.spacefom.util.Matrix;

import referenceFrame.coder.SpaceTimeCoordinateStateCoder;
import skf.coder.HLAunicodeStringCoder;
import skf.model.object.annotations.Attribute;
import skf.model.object.annotations.ObjectClass;

@ObjectClass(name = "ReferenceFrame")
public class ReferenceFrameObject {

	// Implement ReferenceFrame relationship as "Has-A".
	private ReferenceFrame frame = null;

	@Attribute(name = "name", coder = HLAunicodeStringCoder.class)
	private String name = null;

	@Attribute(name = "parent_name", coder = HLAunicodeStringCoder.class)
	private String parent_name = null;

	@Attribute(name = "state", coder = SpaceTimeCoordinateStateCoder.class)
	private SpaceTimeCoordinateState state = null;
	
	private Matrix T_parent_body = null;

	public ReferenceFrameObject(ReferenceFrame frame) {
		this.frame = frame;
		this.T_parent_body = new Matrix(3, 3);
		this.frame.getState().getTime().setEpoch(Ephoc.TJD);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.frame.getName();
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the parent_name
	 */
	public String getParent_name() {
		return (this.frame.getParent() == null)?"":this.frame.getParent().getName();
	}

	/**
	 * @param parent_name the parent_name to set
	 */
	public void setParent_name(String parent_name) {
		this.parent_name = parent_name;
	}

	/**
	 * @return the time
	 */
	public SpaceTimeCoordinateState getState() {
		return this.frame.getState();
	}

	/**
	 * @param time the time to set
	 */
	public void setState(SpaceTimeCoordinateState state) {
		this.frame.setState(state);
	}

	public ReferenceFrame getFrame() {
		return this.frame;
	}
	
	/**
	 * @return the t_parent_body
	 */
	public Matrix getT_parent_body() {
		return T_parent_body;
	}

	/**
	 * @param t_parent_body the t_parent_body to set
	 */
	public void setT_parent_body(Matrix t_parent_body) {
		T_parent_body = t_parent_body;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ReferenceFrameObject [name=" + this.getName() + ", parent_name="
				+ this.getParent_name() + ", state=" + this.getState() + "]";
	}

	
}
