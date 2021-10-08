import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.util.PairConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class CommitHandler extends CheckinHandlerFactory {
    @Override
    public @NotNull CheckinHandler createHandler(@NotNull CheckinProjectPanel checkinProjectPanel, @NotNull CommitContext commitContext) {
        final CheckinHandler ch = new CheckinHandler() {
            @Override
            public ReturnResult beforeCheckin(@Nullable CommitExecutor executor, PairConsumer<Object, Object> additionalDataConsumer) {

                System.out.println("commit catturato  "+commitContext.getUserDataString());
                commitContext.getUserDataString();

                return super.beforeCheckin(executor, additionalDataConsumer);
            }

            @Override
            public void checkinSuccessful() {
                System.out.println("blocco");
                //super.checkinSuccessful();
            }
        };
        return ch;
    }
}
