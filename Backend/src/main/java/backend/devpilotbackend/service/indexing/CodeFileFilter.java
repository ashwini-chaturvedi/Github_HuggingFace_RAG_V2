package backend.devpilotbackend.service.indexing;
//This will filter out the things from file that we don't have to Index

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
public class CodeFileFilter {
    public static final Set<String>SKIP_DIR_PARTS=Set.of(
            "node_modules",
            ".git",
            "dist",
            "build",
            "target",
            ".next",
            "vendor",
            "__pycache__",
            ".idea",
            ".vscode",
            "coverage",
            "out"
    );
    public static final Set<String>SKIP_FILENAMES=Set.of(
            "package-lock.json",
            "yarn.lock",
            "pnpm-lock.yaml",
            "composer.lock",
            "cargo.lock",
            "poetry.lock"
    );

    //Allowed Extensions to be Indexed
    public static final Set<String>ALLOWED_EXTENSIONS=Set.of(
            "java","kt","kts","scala","ts","tsx","js","jsx","mjs","cjs",
            "py","go","rs","rb","php","c","h","cpp","hpp","cs",
            "swift","m","mm","md","mdx","txt","yml","yaml","toml","json","xml",
            "properties","gradle","mvn","sql","sh","bash","zsh","dockerfile",
            "makefile","html","css","scss","sass","vue","svelte"
    );

    public boolean isEligible(String path,long sizeBytes,long maxFileBytes){
        if(path==null || path.isBlank()) return false;

        String normalized=path.replace('\\','/');
        String lower=normalized.toLowerCase(Locale.ROOT);

        for(String part:lower.split("/")){
            if(SKIP_DIR_PARTS.contains(part)) return false;
        }

        String fileName=lower.substring(lower.lastIndexOf('/')+1);
        if(SKIP_FILENAMES.contains(fileName)) return false;

        if(fileName.startsWith(".")) return false;

        if(sizeBytes>maxFileBytes) return false;

        if("dockerfile".equals(fileName) || "makefile".equals(fileName)) return true;

        int dot=fileName.lastIndexOf('.');
        if(dot<0) return false;

        String fileExtension=fileName.substring(dot+1);

        return ALLOWED_EXTENSIONS.contains(fileExtension);
    }

    public String detectLanguage(String path){
        String lower=path.toLowerCase(Locale.ROOT);
        String fileName=lower.substring(lower.lastIndexOf('/')+1);
        if("dockerfile".equals(fileName)) return fileName;

        if("makefile".equals(fileName)) return fileName;

        int dot=fileName.lastIndexOf('.');
        if(dot<0) return "text";
        String fileExtension=fileName.substring(dot+1);
        return fileExtension;
    }



}
