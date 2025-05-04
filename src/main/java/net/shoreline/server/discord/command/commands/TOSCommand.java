package net.shoreline.server.discord.command.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.shoreline.server.discord.command.Command;

import java.awt.*;

public final class TOSCommand extends Command
{
    public TOSCommand()
    {
        super("tos");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event,
                        EmbedBuilder builder)
    {
        builder.setTitle("Terms of Service");
        builder.addField("1. Introduction & Acceptance of Terms", "By accessing or using Shoreline, you agree to be bound by these Terms of Service (\"Terms\"). If you do not agree to these Terms, you will not be permitted to purchase or use our digitally supplied goods (\"software\"). Shoreline (\"we\", \"us\", or \"our\") reserves the right to modify these Terms at any time without prior notice. Continued use of our service after any such changes constitutes your acceptance of the new Terms.\n", false);
        builder.addField("2. Eligibility & Access", "To use our software, you must be at least 18 years old and have the legal capacity to enter into a binding agreement. By purchasing or accessing our digital products, you represent and warrant that you meet these eligibility requirements.\n", false);
        builder.addField("3. User Obligations & Responsibilities", "You agree not to engage in any activities that violate these Terms, including but not limited to:\n" +
                "- Unauthorized distribution or sharing of the software.\n" +
                "- Attempting to dump, reverse engineer, or crack the software.\n" +
                "- Any otherwise misuse of the software or any of its associated binaries. \n" +
                "All content provided within our software, including but not limited to graphics, logos, and any distributed binaries, is the intellectual property of Shoreline. You are granted a non-exclusive, non-transferable license to use the software for personal use only.\n", false);
        builder.addField("4. Intellectual Property", "\n" +
                "The software, including all associated intellectual property rights, are and shall remain the exclusive property of Shoreline. You do not acquire any ownership rights by purchasing or using our software. \n" +
                "The software, including all associated intellectual property rights, are and shall remain the exclusive property of Shoreline. You do not acquire any ownership rights by purchasing or using our software.\n", false);
        builder.addField("5. Refund Policy", "Due to the nature of the software, all sales are final, and no refunds will be provided once the product has been accessed or downloaded. By purchasing, you acknowledge and agree that you understand this no-refund policy.", false)
                .addField("Exceptions", "A refund will be granted if:\n" +
                        "- If you do not receive the software due to technical issues on our end, and we are unable to resolve the issue.\n" +
                        "- If the software is found to be defective or unusable to the extent that you are unable to access it due to a technical issue that we are unable to fix.\n" +
                        "- If a purchase was made without your authorization (e.g., fraudulent activity).", false)
                .addField("Requesting a Refund", "To request a refund under the exceptions listed above, please contact our support team through discord within 14 days of purchase. Your request will be reviewed, and if it meets the criteria, a refund will be issued.", false)
                .addField("Chargeback Prohibition", "You agree not to initiate a chargeback with your credit card provider or payment processor. If a chargeback is initiated, Shoreline reserves the right to dispute the chargeback and take necessary legal action to recover any lost funds, including any additional fees incurred.\n", false);
        builder.addField("6. Delivery Confirmation", "Shoreline maintains records of all purchases, downloads, and accesses to the software. These records may be used as evidence of delivery and usage if a dispute arises. By accessing the software, you acknowledge that the product has been delivered as described and to your satisfaction.\n", false);
        builder.addField("7. Disclaimers & Limitation of Liability", "The digital product is provided \"as is\" without any warranties, express or implied. Shoreline does not guarantee that the software will be error-free.", false)
                .addField("Limitation of Liability", "\nIn no event shall Shoreline be liable for any indirect, incidental, or consequential damages arising out of the use or inability to use the software, even if advised of the possibility of such damages.\n", false);
        builder.addField("8. Indemnification", "You agree to indemnify and hold harmless Shoreline from any claims, damages, or expenses arising out of your use of the software, including any violations of these Terms.\n", false);
        builder.addField("9. Termination of Service", "Shoreline reserves the right to terminate your access to the software at any time, without notice, for conduct that violates these Terms.\n", false);
        builder.addField("10. Privacy & Data Protection", "\n" +
                "When purchasing Shoreline and creating an account, our payment service forwards us your email used for the purchase so that we may send you a License Key to access the software. Shoreline does not store this email. When creating an account, you supply a username and a password. Shoreline stores your username and a hashed version of your password.\n" +
                "When accessing the software, Shoreline collects various pieces of information about your physical computer which are encrypted/hashed and used to authorize you.\n" +
                "Shoreline will NEVER store your plaintext password or any other personal information.\n", false);
        builder.addField("11. Notices", "All notices required or permitted under these Terms shall be in writing and announced in our Discord. You agree to receive communications from us electronically.\n", false);
        builder.addField("12. Entire Agreement", "These Terms constitute the entire agreement between you and Shoreline regarding the software and supersede all prior agreements and understandings.\n", false);
        builder.addField("13. Compliance with Laws", "You agree to comply with all applicable local, national, and international laws when using our digital products.\n", false);

        builder.setColor(new Color(39, 127, 196));
    }
}
