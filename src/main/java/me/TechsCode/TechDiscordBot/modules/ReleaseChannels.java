package me.TechsCode.TechDiscordBot.modules;

import me.TechsCode.TechDiscordBot.Module;
import me.TechsCode.TechDiscordBot.Query;
import me.TechsCode.TechDiscordBot.TechDiscordBot;
import me.TechsCode.TechDiscordBot.objects.DefinedQuery;
import me.TechsCode.TechDiscordBot.objects.Requirement;
import me.TechsCode.TechDiscordBot.util.CustomEmbedBuilder;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class ReleaseChannels extends Module {

    private final DefinedQuery<TextChannel> CHANNELS = new DefinedQuery<TextChannel>() {
        @Override
        protected Query<TextChannel> newQuery() {
            return bot.getChannels("releases");
        }
    };

    public ReleaseChannels(TechDiscordBot bot) {
        super(bot);
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public String getName() {
        return "Release Channels";
    }

    @Override
    public Requirement[] getRequirements() {
        return new Requirement[]{
                new Requirement(CHANNELS, 1, "Requires at least one #releases Channel")
        };
    }

    @SubscribeEvent
    public void receive(GuildMessageReceivedEvent e) {
        if(e.getAuthor().isBot()) return;

        if(!CHANNELS.query().all().contains(e.getChannel())){
            return;
        }

        if(e.getMessage().getAttachments().size() != 1){
            e.getMessage().delete().queue();
            return;
        }

        Message.Attachment attachment = e.getMessage().getAttachments().get(0);

        if(!attachment.getFileName().endsWith(".jar")){
            new CustomEmbedBuilder("File Not Accepted!").setText("The file type must be a jar file.").sendTemporary(e.getChannel(), 5, TimeUnit.SECONDS);
            e.getMessage().delete().queue();
            return;
        }

        File file = new File(attachment.getFileName());
        file.delete();
        attachment.download(file);
        file.deleteOnExit();

        Role testersRole = bot.getRoles(e.getMessage().getCategory().getName()).first();
        String testersMention = testersRole != null ? testersRole.getAsMention() : "everyone";

        TextChannel feedbackChannel = bot.getChannels("feedback").inCategory(e.getMessage().getCategory()).first();
        String feedbackMention = feedbackChannel != null ? feedbackChannel.getAsMention() : "**#feedback**";

        Emote upvoteEmote = bot.getEmotes("upvote").first();
        Emote downvoteEmote = bot.getEmotes("downvote").first();

        CustomEmbedBuilder builder = new CustomEmbedBuilder("New Release")
                .setText("**Hello** "+testersMention+ " **Testers!** \n" +
                        "\n" +
                        "A new file has just been submitted for testing.\n" +
                        "Please test every change carefully and give us feedback in "+feedbackMention+"\n" +
                        "\n" +
                        "Make sure to **react** to let us know if the changes are working.")
                .addField("Changes", e.getMessage().getContentDisplay(), false);

        Message message = builder.send(e.getChannel());
        e.getChannel().sendFile(file).queue();

        if(testersRole != null){
            Message mentionMessage = e.getChannel().sendMessage(testersRole.getAsMention()).complete();
            mentionMessage.delete().queueAfter(3, TimeUnit.SECONDS);
        }

        if(feedbackChannel != null){
            feedbackChannel.sendMessage(new EmbedBuilder().setDescription("A new file has just been released in " + e.getChannel().getAsMention() + "!").build()).submit();
        }

        if(upvoteEmote != null && downvoteEmote != null){
            message.addReaction(upvoteEmote).submit();
            message.addReaction(downvoteEmote).submit();
        }

        e.getMessage().delete().queue();
    }
}
