package com.jeltechnologies.photos.picures.frame;

import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.picures.frame.program.BaseFrameProgram;

public record FilterOption(User user, BaseFrameProgram program, String photoIdInSlideShow, int amount, Contents contents, int programPercentage) {
    public enum Contents {
	PROGRAM_ONLY, PROGRAM_WITH_RANDOM, RANDOM_WITH_PROGRAM
    }
}
