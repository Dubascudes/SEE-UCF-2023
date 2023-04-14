package interaction;

import coder.HLAGridIndexCoder;
import skf.coder.HLAinteger64LECoder;
import skf.model.interaction.annotations.InteractionClass;
import skf.model.interaction.annotations.Parameter;
import state.SimulationEntityState.GridIndex;

/**
 * Created by rick on 2/8/17.
 */
@InteractionClass(name="PathFindingRequest")
public class PathFindingRequest extends Interaction {
    @Parameter(name="fromHLAId", coder=HLAinteger64LECoder.class)
    protected Long fromHLAId = null;

    @Parameter(name="toHLAId", coder=HLAinteger64LECoder.class)
    protected Long toHLAId = null;

    @Parameter(name="location", coder= HLAGridIndexCoder.class)
    private GridIndex location;

    public PathFindingRequest(long hlaID, GridIndex location) {
        super(hlaID);
        setLocation(location);
    }

    public PathFindingRequest() {
        super(null);
    }

    public GridIndex getLocation() {
        return location;
    }

    public void setLocation(GridIndex location) {
        this.location = location;
    }
}
