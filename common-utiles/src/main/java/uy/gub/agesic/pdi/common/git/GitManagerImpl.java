package uy.gub.agesic.pdi.common.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uy.gub.agesic.pdi.common.exceptions.PDIException;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

@Component
public class GitManagerImpl implements GitManager {

    private static Logger logger = LoggerFactory.getLogger(GitManagerImpl.class);

    private Git git;
    private Repository localRepository;

    @Value("${application.git.localPath:none}")
    private String localPath;

    @Value("${application.git.remotePath:none}")
    private String remotePath;

    @Value("${application.git.username:none}")
    private String username;

    @Value("${application.git.password:none}")
    private String password;

    @PostConstruct
    private void init() {
        if (localPath != null && !localPath.equals("none")) {
            try {
                this.localRepository = new FileRepository(this.localPath + "/.git");
                this.git = new Git(this.localRepository);

                File directorio = new File(localPath);
                if (directorio.exists()) {
                    FileUtils.delete(directorio, FileUtils.RECURSIVE);
                }

                // Si el directorio no existe, el repositorio debe clonarse
                if (!directorio.exists()) {
                    directorio.mkdir();
                    this.clonarRepositorio();
                }

            } catch (PDIException e) {
                logger.error("Ha ocurrido un error inicializando el manager de GIT", e);
            } catch (IOException e) {
                logger.error("Ha ocurrido un error inicializando el manager de GIT", e);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Metodos auxiliares

    private void add(String fileName) throws IOException, GitAPIException {
        File gitFile = new File(this.localPath + fileName);
        if (!gitFile.exists()) {
            gitFile.createNewFile();
        }

        git.add()
           .addFilepattern(fileName)
           .call();
    }

    private void commit(String message) throws IOException, GitAPIException, JGitInternalException {
        git.commit()
                .setMessage(message)
                .call();
    }

    private void push() throws IOException, JGitInternalException, GitAPIException {
        git.push()
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username,password))
                .call();
    }

    private void pull() throws GitAPIException {
        git.pull()
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username,password))
                .call();

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void clonarRepositorio() throws PDIException {
        try {
            Git.cloneRepository()
                    .setURI(this.remotePath)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(this.username, this.password))
                    .setDirectory(new File(this.localPath))
                    .call();
        } catch (GitAPIException e) {
            throw new PDIException("Ha ocurrido un error salvando los cambios en el repositorio GIT", e);
        }
    }

    public void enviarCambios(String message, String fileName) throws PDIException {
        try {
            this.add(fileName);
            this.commit(message);
            this.push();
        } catch (IOException e) {
            throw new PDIException("Ha ocurrido un error salvando los cambios en el repositorio GIT REMOTO", e);
        } catch (GitAPIException e) {
            throw new PDIException("Ha ocurrido un error salvando los cambios en el repositorio GIT REMOTO", e);
        }
    }

    public void recibirCambios() throws PDIException {
        try {
            this.pull();
        } catch (GitAPIException e) {
            throw new PDIException("Ha ocurrido un error actualizando los cambios en el repositorio GIT LOCAL", e);
        }
    }

    @Override
    public String getAbsoluteLocalPath(String fileName) throws PDIException {
        if (fileName == null || "".equalsIgnoreCase(fileName)) {
            throw new PDIException("La ruta indicada para el archivo local no es valida, no puede ser nula");
        }

        return this.localPath + fileName;
    }
}

