package interaction;

import coder.HLAGridIndexCoder;
import skf.coder.HLAinteger64LECoder;
import skf.model.interaction.annotations.InteractionClass;
import skf.model.interaction.annotations.Parameter;
import state.SimulationEntityState.GridIndex;


/**
 * @author Will English
 * Based on ProspectingRover by Khris
 * 
 */
@InteractionClass(name = "ReflectorLocationResponse")
public class ReflectorLocationResponse extends Interaction {

    @Parameter(name="fromHLAId", coder=HLAinteger64LECoder.class)
    protected Long fromHLAId = null;

    @Parameter(name="toHLAId", coder=HLAinteger64LECoder.class)
    protected Long toHLAId = null;

    @Parameter(coder = HLAGridIndexCoder.class, name = "reflectorSite")
    private GridIndex reflectorSite;

    public ReflectorLocationResponse(){
        super(null);
    }

    public ReflectorLocationResponse(Long hlaId, GridIndex reflectorSite){
        super(hlaId);
        this.setReflectorSite(reflectorSite);
    }

    public ReflectorLocationResponse(Long from, Long to, GridIndex reflectorSite){
        super(from, to);
        this.setReflectorSite(reflectorSite);
    }

    public GridIndex getReflectorSite() {
        return reflectorSite;
    }

    public void setReflectorSite(GridIndex reflectorSite) {
        this.reflectorSite = reflectorSite;
    }

}
