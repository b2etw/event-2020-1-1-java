package tw.b2e.news;

import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tw.b2e.common.BlocksResponseRouter;
import tw.b2e.news.ptt.entity.Board;
import tw.b2e.news.ptt.entity.PttCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class NewsRouter implements BlocksResponseRouter<SlashCommandRequest> {

  // todo - I will refactor it. 人肉爬蟲 XD
  private static final String PTT_BASE_URL = "https://www.ptt.cc/bbs/";
  private static final String INDEX = "/index.html";

  // persisted
  private static final List<Board> BOARDS = new ArrayList<>();

  static {
    BOARDS.add(new Board("gossiping", 26432, "綜合", "◎【八卦】版主投票開始囉！", pttUrl("gossiping")));
    BOARDS.add(new Board("baseball", 4325, "棒球", "◎[棒球] 中職31年開幕", pttUrl("baseball")));
    BOARDS.add(new Board("c_chat", 3685, "閒談", "◎[希洽] 系列文監控中= =", pttUrl("c_chat")));
    BOARDS.add(new Board("stock", 4325, "學術", "◎3/30 0：00起 板規處分提高一級", pttUrl("stock")));
    BOARDS.add(new Board("beauty", 696, "聊天", "◎《表特板》發文附圖", pttUrl("beauty")));
  }

  private CommandService commandService;
  private ReplyService replyService;

  @Autowired
  NewsRouter(CommandService commandService,
             ReplyService replyService) {
    this.commandService = commandService;
    this.replyService = replyService;
  }

  private static String pttUrl(final String code) {
    return PTT_BASE_URL + code + INDEX;
  }

  public List<LayoutBlock> handle(SlashCommandRequest req) {
    log.info("req: {}", req);

    if (Objects.isNull(req)
        || Objects.isNull(req.getPayload())
        || Objects.isNull(req.getPayload().getText())
        || req.getPayload().getText().isEmpty()) {
      // oops
      return Collections.singletonList(sectionBlock("Mewo~"));
    }

    PttCommand command = commandService.wrapCommand(req.getPayload().getText());

    // parser command
    if (command.getMainOption().contains("list")) {
      // todo - data crawler

      // simple response
      return replyService.reply(BOARDS);
    }

    // default response
    return Collections.singletonList(sectionBlock(command.getMainOption()));
  }

  private SectionBlock sectionBlock(final String msg) {
    SectionBlock section = new SectionBlock();
    section.setText(new MarkdownTextObject(msg, false));
    return section;
  }
}
