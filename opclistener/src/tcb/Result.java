package tcb;

import org.openscada.opc.lib.da.ItemState;

public class Result {
    private String itemId;// 监控位置
    private ItemState state;// 监控值

    public Result(String itemId, ItemState state) {
        this.itemId = itemId;
        this.state = state;
    }

    public String getItemId() {
        return itemId;
    }

    public ItemState getItemState() {
        return state;
    }

}
