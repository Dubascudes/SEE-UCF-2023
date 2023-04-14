package interaction;

import coder.HLAGridIndexCoder;
import exploration.Grid;
import skf.coder.HLAinteger64LECoder;
import skf.model.interaction.annotations.InteractionClass;
import skf.model.interaction.annotations.Parameter;
import state.SimulationEntityState;
 

/**
 * @author Will English
 */
@InteractionClass(name = "ReflectorInteractionRequest")
public class ReflectorInteractionRequest extends Interaction {

    @Parameter(coder = HLAinteger64LECoder.class, name = "fromHLAId")
    protected Long fromHLAId = null;

    @Parameter(coder = HLAinteger64LECoder.class, name = "toHLAId")
    protected Long toHLAId = null;

    public ReflectorInteractionRequest(Long hlaId) {
        super(hlaId);
    }

    public ReflectorInteractionRequest() {
        super(null);
    }

    public ReflectorInteractionRequest(Long fromHLAId, Long toHLAId) {
        super(fromHLAId, toHLAId);
    }

}
