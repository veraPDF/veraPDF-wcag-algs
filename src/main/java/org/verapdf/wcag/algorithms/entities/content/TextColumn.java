package org.verapdf.wcag.algorithms.entities.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TextColumn extends TextInfoChunk {

    private final List<TextBlock> textBlocks = new ArrayList<>();

    public TextColumn() {
    }

    public TextColumn(TextLine line) {
        textBlocks.add(new TextBlock(line));
    }

    public TextColumn(TextColumn column) {
        super(column.getBoundingBox(), column.getFontSize(), column.getBaseLine());
        textBlocks.addAll(column.getBlocks());
    }

    public List<TextBlock> getBlocks() {
        return textBlocks;
    }

	public int getBlocksNumber() {
		return textBlocks.size();
	}


	public TextBlock getFirstTextBlock() {
    	if (!textBlocks.isEmpty()) {
    		return textBlocks.get(0);
	    }
    	return null;
    }

	public TextBlock getLastTextBlock() {
		if (!textBlocks.isEmpty()) {
			return textBlocks.get(textBlocks.size() - 1);
		}
		return null;
	}

	public void setLastTextBlock(TextBlock block) {
    	if (!textBlocks.isEmpty()) {
    		textBlocks.set(textBlocks.size() - 1, block);
	    } else {
		    textBlocks.add(block);
	    }
	}

	public TextBlock getSecondTextBlock() {
		if (textBlocks.size() > 1) {
			return textBlocks.get(1);
		}
		return null;
	}

	public TextBlock getPenultTextBlock() {
		if (textBlocks.size() > 1) {
			return textBlocks.get(textBlocks.size() - 2);
		}
		return null;
	}

	public List<TextLine> getLines() {
    	List<TextLine> textLines = new ArrayList<>();
    	for (TextBlock textBlock : textBlocks) {
    		textLines.addAll(textBlock.getLines());
	    }
		return textLines;
	}

    public TextLine getFirstLine() {
        if (textBlocks.isEmpty()) {
            return null;
        }
        return textBlocks.get(0).getFirstLine();
    }

    public TextLine getLastLine() {
        if (textBlocks.isEmpty()) {
            return null;
        }
        return textBlocks.get(textBlocks.size() - 1).getLastLine();
    }

    public void setLastLine(TextLine lastLine) {
        if (!textBlocks.isEmpty()) {
            textBlocks.get(textBlocks.size() - 1).setLastLine(lastLine);
        } else {
            textBlocks.add(new TextBlock(lastLine));
        }
    }

    public void setFirstLine(TextLine firstLine) {
        if (!textBlocks.isEmpty()) {
            textBlocks.get(0).setFirstLine(firstLine);
        } else {
            textBlocks.add(new TextBlock(firstLine));
        }
    }

    public TextLine getSecondLine() {
        if (!textBlocks.isEmpty()) {
            TextBlock firstBlock =  getFirstTextBlock();
            if (firstBlock.getLinesNumber() > 1) {
                return firstBlock.getSecondLine();
            }
            if (textBlocks.size() > 1) {
            	return getSecondTextBlock().getFirstLine();
            }
        }
        return null;
    }

    public TextLine getPenultLine() {
	    if (!textBlocks.isEmpty()) {
		    TextBlock lastBlock =  getLastTextBlock();
		    if (lastBlock.getLines().size() > 1) {
			    return lastBlock.getPenultLine();
		    }
		    if (textBlocks.size() > 1) {
		    	return getPenultTextBlock().getLastLine();
		    }
	    }
        return null;
    }

    public void add(TextLine line) {
        textBlocks.add(new TextBlock(line));
        super.add(line);
    }

    public int getLinesNumber() {
        return textBlocks.stream().mapToInt(TextBlock::getLinesNumber).sum();
    }

    public void add(TextColumn column) {
        textBlocks.addAll(column.getBlocks());
        super.add(column);
    }

    public boolean isEmpty() {
        return textBlocks.isEmpty() || textBlocks.stream().allMatch(TextBlock::isEmpty);
    }

    public boolean hasOnlyOneBlock() {
    	return textBlocks.size() == 1;
    }

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder("");
		for (TextBlock textBlock : textBlocks) {
			stringBuilder.append(textBlock);
		}
		return stringBuilder.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (!super.equals(o)) {
			return false;
		}
		if (!(o instanceof TextColumn)) {
			return false;
		}
		TextColumn that = (TextColumn) o;
		return this.textBlocks.equals(that.textBlocks);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), textBlocks);
	}
}
