package gregtech.api.ec;

public interface IRecipePollingComponent extends IComponent {

    ComponentKey.ListKey<IRecipePollingComponent> KEY = ComponentKey.list();

    boolean shouldPollRecipe(long totalRuntime, long timeElapsed);
}
