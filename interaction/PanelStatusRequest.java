package interaction;

import coder.HLAGridIndexCoder;
import skf.coder.HLAfloat64LECoder;
import skf.coder.HLAinteger64LECoder;
import skf.coder.HLAbooleanCoder;
import skf.model.interaction.annotations.Parameter;
import skf.model.interaction.annotations.InteractionClass;
import state.SimulationEntityState.GridIndex;
/*
* Created by Louis
*/
@InteractionClass(name="PanelStatusRequest")
public class PanelStatusRequest extends Interaction {
    @Parameter(name="fromHLAId", coder= HLAinteger64LECoder.class)
    private Long fromHLAId = null;

    @Parameter(name="toHLAId", coder=HLAinteger64LECoder.class)
    private Long toHLAId = null;

    @Parameter(name="position", coder=HLAGridIndexCoder.class)
    private GridIndex position = null;

    @Parameter(name="time", coder=HLAfloat64LECoder.class)
    private Double time = null;
    
    @Parameter(name="elecOutput", coder=HLAfloat64LECoder.class)
    private Double output = null;
    
    @Parameter(name="sunlight", coder=HLAbooleanCoder.class)
    private Boolean isReceiving = null;
    
    public PanelStatusRequest()
    {
    	super(null);
    }
    
    public PanelStatusRequest(Long hlaId, GridIndex position){
        super(hlaId);
        this.fromHLAId = hlaId;
        this.position = position;
    }

    public GridIndex getPosition(){
        return this.position;
    }

    public void setPosition(GridIndex position){
        this.position = position;
    }

    public void setTime(Double time){
        this.time = time;
    }

    public Double getTime(){
        return this.time;
    }
    
    public void setOutput(Double output) {
    	this.output = output;
    }
    
    public Double getOutput() {
    	return this.output;
    }
    
    public void setSunlight(Boolean sunlight) {
    	this.isReceiving = sunlight;
    }
    
    public Boolean getSunlight() {
    	return this.isReceiving;
    }
}

