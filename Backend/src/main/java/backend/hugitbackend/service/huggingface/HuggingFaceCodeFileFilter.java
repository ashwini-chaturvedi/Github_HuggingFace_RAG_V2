package backend.hugitbackend.service.huggingface;

import backend.hugitbackend.entity.enums.HuggingFaceRepoType;
import backend.hugitbackend.service.indexing.DetectLanguage;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
public class HuggingFaceCodeFileFilter implements DetectLanguage {

    public static final Set<String> HF_SKIP_DIR_PARTS=Set.of(
            ".git",
            ".cache",
            "checkpoint-",
            "runs",
            "logs",
            "wandb",
            "__pycache__",
            ".ipynb_checkpoints"
    );

    public static final Set<String> HF_SKIP_FILENAMES=Set.of(
            ".gitattributes",
            ".gitignore",
            "pytorch_model.bin",
            "tf_model.h5",
            "model.safetensors",
            "flax_model.msgpack",
            "optimizer.pt",
            "training_args.bin",
            "events.out.tfevents"
    );

    public static final Set<String> HF_SPACE_ALLOWED_EXTENSIONS = Set.of(
            "py", "ipynb", "md", "mdx", "txt", "json", "yml", "yaml",
            "toml", "cfg", "ini", "sh", "js", "ts", "html", "css"
    );

    // For MODEL/DATASET repos — the strict allow-list from our design discussion.
    // Filename-based, not extension-based, since these are specific known files, not "any .json"
    public static final Set<String> MODEL_DATASET_ALLOWED_FILENAMES = Set.of(
            "README.md", "config.json", "generation_config.json",
            "tokenizer_config.json", "preprocessor_config.json",
            "dataset_infos.json", "dataset_dict.json"
    );
    public static final Set<String> HF_SKIP_EXTENSIONS=Set.of(
            "bin", "safetensors", "h5", "msgpack", "pt", "pth", "ckpt",
            "onnx", "tflite", "gguf", "ggml", "npz", "npy",
            "arrow", "parquet",     // dataset shards — usually large, not text-indexable
            "zip", "tar", "gz", "7z"
    );

    public boolean isEligible(String path, long sizeBytes, long maxFileBytes,
                              HuggingFaceRepoType repoType){
        if(path==null || path.isBlank()) return false;

        String normalized=path.replace("\\","/");
        String lower=normalized.toLowerCase(Locale.ROOT);
        String fileName=normalized.substring(normalized.lastIndexOf('/')+1);

        if(sizeBytes>maxFileBytes) return false;

        if(repoType==HuggingFaceRepoType.MODEL || repoType==HuggingFaceRepoType.DATASET){
            return MODEL_DATASET_ALLOWED_FILENAMES.contains(fileName);
        }

        for(String part:lower.split("/")){
            if(HF_SKIP_DIR_PARTS.stream().anyMatch(part::startsWith)) return false;
        }

        String lowerFileName=fileName.toLowerCase(Locale.ROOT);
        if(HF_SKIP_FILENAMES.contains(lowerFileName)) return false;
        if(lowerFileName.startsWith(".")) return false;

        int dot=lowerFileName.lastIndexOf('.');
        if(dot<0)return false;

        String extension=lowerFileName.substring(dot+1);
        if(HF_SKIP_EXTENSIONS.contains(extension)) return false;

        return HF_SPACE_ALLOWED_EXTENSIONS.contains(extension);
    }


    @Override
    public String detectLanguage(String path) {
        String lower = path.toLowerCase(Locale.ROOT);
        String fileName = lower.substring(lower.lastIndexOf('/') + 1);
        if ("readme.md".equals(fileName)) return "markdown";
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) return "text";
        return fileName.substring(dot + 1);
    }


}
