package interaction;

import skf.coder.HLAbooleanCoder;
import skf.coder.HLAinteger64LECoder;
import skf.model.interaction.annotations.InteractionClass;
import skf.model.interaction.annotations.Parameter;
import java.lang.Boolean;

/*
 * @author Will English
 */
@InteractionClass(name = "ReflectorInteractionResponse")
public class ReflectorInteractionResponse extends Interaction {

    @Parameter(coder = HLAinteger64LECoder.class, name = "fromHLAId")
    protected Long fromHLAId = null;

    @Parameter(coder = HLAinteger64LECoder.class, name = "toHLAId")
    protected Long toHLAId = null;

    @Parameter(coder = HLAbooleanCoder.class, name = "sunlight")
    private Boolean sunlight;

    public ReflectorInteractionResponse() {
        super(null);
    }

    public ReflectorInteractionResponse(Long fromHLAId, Boolean sunlight) {
        super(fromHLAId);
        this.setSunlight(sunlight);
    }

    public ReflectorInteractionResponse(Long fromHLAId, Long toHLAId, Boolean sunlight) {
        super(fromHLAId, toHLAId);
        this.setSunlight(sunlight);
    }

    public boolean getSunlight() {
        return sunlight;
    }

    public void setSunlight(Boolean sunlight) {
        this.sunlight = sunlight;
    }

}


