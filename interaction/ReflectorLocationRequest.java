package interaction;


import skf.coder.HLAinteger64LECoder;
import skf.model.interaction.annotations.InteractionClass;
import skf.model.interaction.annotations.Parameter;

/*
 * @author Will English 
 * Based on ProspectingRover by Khris
 * 
 */
@InteractionClass(name = "ReflectorLocationRequest")
public class ReflectorLocationRequest extends Interaction {

    @Parameter(coder = HLAinteger64LECoder.class, name = "fromHLAId")
    protected Long fromHLAId = null;

    @Parameter(coder = HLAinteger64LECoder.class, name = "toHLAId")
    protected Long toHLAId = null;

    public ReflectorLocationRequest(Long fromHLAId) {
        super(fromHLAId);
    }

    public ReflectorLocationRequest() {
        super(null);
    }

    public ReflectorLocationRequest(Long fromHLAId, Long toHLAId) {
        super(fromHLAId, toHLAId);
    }

}
