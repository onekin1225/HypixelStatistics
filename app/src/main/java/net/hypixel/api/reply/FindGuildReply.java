package net.hypixel.api.reply;

/**
 * Created by AgentK on 10/11/2014, 5:51 PM
 * for Hypixel Statistics in package net.hypixel.api.reply
 */
@SuppressWarnings("unused")
public class FindGuildReply extends AbstractReply {
    private String guild;

    /**
     * @return The ID of the guild that was found, or null if there was no guild by that name
     */
    public String getGuild() {
        return guild;
    }

    @Override
    public String toString() {
        return "FindGuildReply{" +
                "guild=" + guild +
                ",super=" + super.toString() + "}";
    }
}
