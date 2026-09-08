package gregtech.api.ec;

public interface IRecipePollingComponent extends IComponent {
    boolean shouldPollRecipe(long totalRuntime, long timeElapsed);
}
