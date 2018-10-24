package featurecat.lizzie.gui;

import featurecat.lizzie.Lizzie;
import featurecat.lizzie.rules.BoardHistoryNode;
import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;

public class VariationTree {

  private int YSPACING;
  private int XSPACING;
  private int DOT_DIAM = 11; // Should be odd number
  private int CENTER_DIAM = 5;
  private int RING_DIAM = 15;

  private ArrayList<Integer> laneUsageList;
  private BoardHistoryNode curMove;

  public VariationTree() {
    laneUsageList = new ArrayList<Integer>();
  }

  public void drawTree(
      Graphics2D g,
      int posx,
      int posy,
      int startLane,
      int maxposy,
      int minposx,
      BoardHistoryNode startNode,
      int variationNumber,
      boolean isMain) {
    if (isMain) g.setColor(Color.white);
    else g.setColor(Color.gray.brighter());

    // Finds depth on leftmost variation of this tree
    int depth = startNode.getDepth() + 1;
    int lane = startLane;
    // Figures out how far out too the right (which lane) we have to go not to collide with other
    // variations
    int moveNumber =
        isMain
            ? startNode.getData().moveNumber
            : startNode.findTop().findMoveNumberOfNode(startNode);
    while (lane < laneUsageList.size() && laneUsageList.get(lane) <= moveNumber + depth) {
      // laneUsageList keeps a list of how far down it is to a variation in the different "lanes"
      laneUsageList.set(lane, moveNumber - 1);
      lane++;
    }
    if (lane >= laneUsageList.size()) {
      laneUsageList.add(0);
    }
    if (variationNumber > 1) laneUsageList.set(lane - 1, moveNumber - 1);
    laneUsageList.set(lane, moveNumber);

    // At this point, lane contains the lane we should use (the main branch is in lane 0)

    BoardHistoryNode cur = startNode;
    int curposx = posx + lane * XSPACING;
    int dotoffset = DOT_DIAM / 2;

    // Draw line back to main branch
    if (lane > 0) {
      if (lane - startLane > 0 || variationNumber > 1) {
        // Need a horizontal and an angled line
        drawLine(
            g,
            curposx + dotoffset,
            posy + dotoffset,
            curposx + dotoffset - XSPACING,
            posy + dotoffset - YSPACING,
            minposx);
        drawLine(
            g,
            posx + (startLane - variationNumber) * XSPACING + 2 * dotoffset,
            posy - YSPACING + dotoffset,
            curposx + dotoffset - XSPACING,
            posy + dotoffset - YSPACING,
            minposx);
      } else {
        // Just an angled line
        drawLine(
            g,
            curposx + dotoffset,
            posy + dotoffset,
            curposx + 2 * dotoffset - XSPACING,
            posy + 2 * dotoffset - YSPACING,
            minposx);
      }
    }

    // Draw all the nodes and lines in this lane (not variations)
    Color curcolor = g.getColor();
    if (curposx > minposx) {
      if (startNode.previous().isPresent()) {
        if (!cur.getData().comment.isEmpty()) {
          g.setColor(Lizzie.config.commentNodeColor);
          g.fillOval(
              curposx + (DOT_DIAM - RING_DIAM) / 2,
              posy + (DOT_DIAM - RING_DIAM) / 2,
              RING_DIAM,
              RING_DIAM);
        }
        g.setColor(Lizzie.frame.getBlunderNodeColor(cur));
        g.fillOval(curposx, posy, DOT_DIAM, DOT_DIAM);
        if (startNode == curMove) {
          g.setColor(Color.BLACK);
          g.fillOval(
              curposx + (DOT_DIAM - CENTER_DIAM) / 2,
              posy + (DOT_DIAM - CENTER_DIAM) / 2,
              CENTER_DIAM,
              CENTER_DIAM);
        }
      } else {
        g.fillRect(curposx, posy, DOT_DIAM, DOT_DIAM);
        g.setColor(Color.BLACK);
        g.drawRect(curposx, posy, DOT_DIAM, DOT_DIAM);
      }
    }
    g.setColor(curcolor);

    // Draw main line
    while (cur.next().isPresent() && posy + YSPACING < maxposy) {
      posy += YSPACING;
      cur = cur.next().get();
      if (curposx > minposx) {
        if (!cur.getData().comment.isEmpty()) {
          g.setColor(Lizzie.config.commentNodeColor);
          g.fillOval(
              curposx + (DOT_DIAM - RING_DIAM) / 2,
              posy + (DOT_DIAM - RING_DIAM) / 2,
              RING_DIAM,
              RING_DIAM);
        }
        g.setColor(Lizzie.frame.getBlunderNodeColor(cur));
        g.fillOval(curposx, posy, DOT_DIAM, DOT_DIAM);
        if (cur == curMove) {
          g.setColor(Color.BLACK);
          g.fillOval(
              curposx + (DOT_DIAM - CENTER_DIAM) / 2,
              posy + (DOT_DIAM - CENTER_DIAM) / 2,
              CENTER_DIAM,
              CENTER_DIAM);
        }
        g.setColor(curcolor);
        g.drawLine(
            curposx + dotoffset,
            posy - 1,
            curposx + dotoffset,
            posy - YSPACING + 2 * dotoffset + 2);
      }
    }
    // Now we have drawn all the nodes in this variation, and has reached the bottom of this
    // variation
    // Move back up, and for each, draw any variations we find
    while (cur.previous().isPresent() && cur != startNode) {
      cur = cur.previous().get();
      int curwidth = lane;
      // Draw each variation, uses recursion
      for (int i = 1; i < cur.numberOfChildren(); i++) {
        curwidth++;
        // Recursion, depth of recursion will normally not be very deep (one recursion level for
        // every variation that has a variation (sort of))
        Optional<BoardHistoryNode> variation = cur.getVariation(i);
        if (variation.isPresent()) {
          drawTree(g, posx, posy, curwidth, maxposy, minposx, variation.get(), i, false);
        }
      }
      posy -= YSPACING;
    }
  }

  public void draw(Graphics2D g, int posx, int posy, int width, int height) {
    if (width <= 0 || height <= 0) {
      return; // we don't have enough space
    }

    // Use dense tree for saving space if large-subboard
    YSPACING = (Lizzie.config.showLargeSubBoard() ? 20 : 30);
    XSPACING = YSPACING;

    // Draw background
    g.setColor(new Color(0, 0, 0, 60));
    g.fillRect(posx, posy, width, height);

    // draw edge of panel
    int strokeRadius = 2;
    g.setStroke(new BasicStroke(2 * strokeRadius));
    g.drawLine(
        posx + strokeRadius,
        posy + strokeRadius,
        posx + strokeRadius,
        posy - strokeRadius + height);
    g.setStroke(new BasicStroke(1));

    int middleY = posy + height / 2;
    int xoffset = 30;
    laneUsageList.clear();

    curMove = Lizzie.board.getHistory().getCurrentHistoryNode();

    // Is current move a variation? If so, find top of variation
    BoardHistoryNode top = curMove.findTop();
    int curposy =
        middleY
            - YSPACING
                * top.findMoveNumberOfNode(
                    curMove); // (curMove.getData().moveNumber - top.getData().moveNumber);
    // Go to very top of tree (visible in assigned area)
    BoardHistoryNode node = top;
    while (curposy > posy + YSPACING && node.previous().isPresent()) {
      node = node.previous().get();
      curposy -= YSPACING;
    }

    // Main trunk
    int lane = 0;
    BoardHistoryNode next = node;
    int nexty = curposy;
    while (next.next().isPresent() && nexty + YSPACING < posy + height) {
      nexty += YSPACING;
      next = next.next().get();
    }
    // All the nodes in this variation
    int maxlane = 0;
    while (next.previous().isPresent() && next != node) {
      next = next.previous().get();
      // Each variation, uses recursion
      maxlane = maxlane + next.numberOfChildren() - 1;
      if (next.findIndexOfNode(curMove, true) > 0) {
        lane = maxlane;
      }
    }
    int startx = posx + xoffset;
    if (((lane + 1) * XSPACING + xoffset - width) > 0) {
      startx = startx - ((lane + 1) * XSPACING + xoffset - width);
    }
    drawTree(g, startx, curposy, 0, posy + height, posx + strokeRadius, node, 0, true);
  }

  private void drawLine(Graphics g, int x1, int y1, int x2, int y2, int minx) {
    if (x1 <= minx && x2 <= minx) {
      return;
    }
    int nx1 = x1, ny1 = y1, nx2 = x2, ny2 = y2;
    if (x1 > minx && x2 <= minx) {
      ny2 = y2 - (x1 - minx) / (x1 - x2) * (y2 - y1);
      nx2 = minx;
    } else if (x2 > minx && x1 <= minx) {
      ny1 = y1 - (x2 - minx) / (x2 - x1) * (y1 - y2);
      nx1 = minx;
    }
    g.drawLine(nx1, ny1, nx2, ny2);
  }
}
