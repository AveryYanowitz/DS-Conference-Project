# automatic-english-glosses
This program allows the user to input any sentence and outputs all possible parsings of that sentence. It does this by abstracting from the tags listed in the Brown Corpus.

## Getting Started
This program has two packages: `file_processing` and `main`. The former package's main() function, which is in the `ReaderWriter` class, takes the data from the Brown corpus (stored in `brown_tag.txt`), reads it in, and outputs it to the `.ser` files. This has already been done, meaning the `main` package is ready to use out-of-the-box. You only need to re-run `ReaderWriter` if you change any of the files in the `file_processing` package.

## How It's Made

### Serialization
Because reading and processing in the 1-million word document is very slow—and gives the same output every time—I opted to serialize this output to the `.ser` files. The `ReaderWriter` class takes care of most of this. However, all the maps are TreeMaps underlyingly, and Comparators are not serializable. To get around this, I also added the `SerializableComparator` wrapper class.

Each serialized file holds a different map. `wordsToTagProbs.ser` maps words (as strings) to a set of tags and the probabilities of that word appearing with any given tag. `legalNextTags.ser` maps tags (also strings) to the set of tags that can come immediately after them. `abbrevationKey.ser` maps the short form of tags ("NP", "NN," "VB," etc.) to their unabbreviated forms ("Proper Noun", "Noun", "Verb", etc.) All three of these maps are then stored in a TagAtlas object (see below.)

### Tagging
All possible tags are listed in `all_tags.txt`. The Brown Corpus itself sorts tags into general categories based on their first two letters, and more specifically after that. I originally offered the user the ability to choose between each individual tag, but this proved overwhelming and unhelpful, so I pared it down to just these broad categories. They can select either abbreviated or long-form tags; the program always uses abbreviations under the hood, but it will reformat output in accordance with their choice. (See **TagAtlas** below.) 

Once the user indicates which tags to use, they're given the option to use premade sentences instead of custom ones. I drew these sentences from various sources, and tried to mix up the lengths and vocabulary. They're meant mainly for debugging and quick illustrative purposes; parsing custom sentences is this project's intended mode.

### Parse Trees
In an earlier draft of this project, possible "timelines"—that is, a list of tags for the given sentence, up to a certain word—were stored in a Set<List<String>>. But this was largely redundant, since most timelines were the same for the first few words. It was also difficult to reason about, since it meant making many changes to many mutable objects, all stored within another object that was also mutable. Since all of these parses need to be stored by one object, and since they differ at branching paths, it seemed simpler and more elegant to use an n-ary tree instead. This came with its own complications, of course, but I think it's on the whole a more appropriate data structure, and far easier to read and edit.

### Sentence Probability
The Parse Tree also tracks the probability of each sentence. For any given word *w*, with set of all tags T, this probability is *P(t ∈ T | w)*. The probability of the full sentence is the product of the probabilities of the individual words. However, the product in question is very small, especially for long sentences. To correct for that, I decided to take the nth root of each probability, where n is twice the number of words in the sentence. This keeps the probabilities within a more familiar range. I specifically chose the root function for a few reasons:
   1. It will always preserve probabilities of 0 and 1.
   2. It will always make the decimals larger.
   3. It will preserve the relative size of the probabilities: if P(a) > P(b) before taking the root, P(a) > P(b) afterwards as well.
 
### TagAtlas
To process input, the program has to consult the maps mentioned above in **Serialization**. Rather than pass all of these objects around as parameters, I decided to wrap them in a simple read-only class called TagAtlas. This means they only have to be calculated once (at program launch) and can't be permuted from outside of the class.

## Known Bugs and Limitations

### Slow De-Serialization
De-serialization—done in the TagAtlas program—is very slow in computer time, taking several seconds. That said, it only has to be performed once on each run of the program. I tried to pare it down as much as possible, but from what I've read, Java serialization is just slow. I decided to use it anyway because it's lossless, straightforward, and well-supported by native objects like TreeMaps.

### Unexpected Probabilities
As of now, the program does not consider the transitional probabilities between tags; it only looks at the probabilities of each individual word. Unfortunately, that means the probabilities are not always what a human would expect. For example, take the sentence "The boy runs"—the parsing of "AT NN NNS" is higher than "AT NN VBZ", even though the latter is the only grammatical option. If I continue working on this project in the future, I think adding transitional probabilities would be a good next step.