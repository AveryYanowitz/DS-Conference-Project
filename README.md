# automatic-english-glosses
This program allows the user to input any sentence and outputs all possible parsings of that sentence. It does this by abstracting from the tags listed in the Brown Corpus.

## Getting Started
This program has two packages: `file_reader` and `main`. The former package's main() function, which is in the `Serializer` class, takes the data from the Brown corpus (stored in `brown_tag.txt`), reads it in, and outputs it to the `maps.ser` file. `maps.ser` is already outputted in this folder. Unless you change any of the files in the `file_reader` package, you never need to touch it. The `main` package is the primary one, and its main() function is in the `Tagger` class. This is where the real magic happens, and is probably the only function you'll need to run.

## How It's Made

### Serialization
Because reading and processing in the 1-million word document is very slow—and gives the same output every time—I opted to put this output in an easy-to-read file in `maps.ser`. The `Serializer` class takes care of most of this. However, all the maps are TreeMaps underlyingly, and Comparators are not serializable. To get around this, I also added the `SerializableComparator` wrapper class. `Serializer` writes to and reads from `maps.ser`, which contains two maps: one that connects each word to all the tags it appears with, and one that connects each unique *tag* to all of the tags that can come directly after it.

### Tagging
All possible tags are listed in `all_tags.txt`. The Brown Corpus itself sorts tags into general categories based on their first two letters, and more specifically after that. I originally offered the user the ability to choose between each individual tag, but this proved overwhelming and unhelpful, so I pared it down to just these broad categories. There are few plausible cases where a user would need to include, e.g., semantically superlative adjectives but not morphologically superlative ones, so I don't think this was much of a loss in functionality.

After indicating which tags to include, the user is then given the option to use the premade sentences in `test_sentences.txt`. I drew these sentences from various sources, and tried to mix up the lengths and vocabulary; they're just listed in alphabetical order. They're meant mainly for debugging and quick illustrative purposes; parsing custom sentences is this project's intended mode.

### Parse Trees
In an earlier draft of this project, possible "timelines"—that is, a list of tags for the given sentence, up to a certain word—were stored in a Set<List<String>>. But this was largely redundant, since most timelines were the same for the first few words. It was also difficult to reason about, since it meant making many changes to many mutable objects, all stored within another object that was also mutable. Since all of these parses need to be stored by one object, and since they differ at branching paths, it seemed simpler and more elegant to use an n-ary tree instead. This came with its own complications, of course, but I think it's on the whole a more appropriate data structure, and far easier to read and edit.