package cadastroserver;

import controller.MovimentoJpaController;
import controller.PessoaFisicaJpaController;
import controller.PessoaJuridicaJpaController;
import controller.ProdutoJpaController;
import controller.UsuarioJpaController;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Usuario;
import model.Produto;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.stream.Collectors;
import model.Movimento;
import model.PessoaFisica;
import model.PessoaJuridica;

public class CadastroThreadV2 extends Thread {

    private final UsuarioJpaController ctrlUsu;
    private final ProdutoJpaController ctrl;
    private final MovimentoJpaController ctrlMov;
    private final PessoaFisicaJpaController ctrlPesFisica;
    private final PessoaJuridicaJpaController ctrlPesJuridica;
    private final Socket s1;
    
    public CadastroThreadV2(ProdutoJpaController ctrl, UsuarioJpaController ctrlUsu, MovimentoJpaController ctrlMov, PessoaFisicaJpaController ctrlPesFisica, PessoaJuridicaJpaController ctrlPesJuridica, Socket s1) {
        this.ctrlUsu = ctrlUsu;
        this.ctrl = ctrl;
        this.ctrlMov = ctrlMov;
        this.ctrlPesFisica = ctrlPesFisica;
        this.ctrlPesJuridica = ctrlPesJuridica;
        this.s1 = s1;
    }

   
    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(s1.getOutputStream()); ObjectInputStream in = new ObjectInputStream(s1.getInputStream())) {

            String login = (String) in.readObject();
            String senha = (String) in.readObject();
            Usuario usuarioLogado = null;
            
            try{
                usuarioLogado = BuscarUsuario(login, senha);
            
            }catch( Exception ex){
                enviarMensagemCliente(out, "NAO AUTORIZADO");
                s1.close();
                return;
            }
                 

            enviarMensagemCliente(out, "USUARIO CONECTADO COM SUCESSO");

            while (true) {
                String comando = (String) in.readObject();
                comando = comando.toUpperCase();
                
                switch (comando) {
                    case "L" ->
                        enviarMensagemCliente(out, listarProdutos());
                    case "E", "S" -> {

                        int idPessoa = (int) in.readObject();
                        int idProduto = (int) in.readObject();
                        int quantidade = (int) in.readObject();
                        long valorUnitario = (long) in.readObject();

                        Movimento movimento = new Movimento();
                        
                        if( "E".equals(comando) ){
                           PessoaJuridica pessoaJuridica = ctrlPesJuridica.findPessoaJuridica(idPessoa);
                           movimento.setPessoaidPessoa( pessoaJuridica.getPessoa() );
                        }
                        
                        if( "S".equals(comando) ){
                           PessoaFisica pessoaFisica = ctrlPesFisica.findPessoaFisica(idPessoa);
                           movimento.setPessoaidPessoa( pessoaFisica.getPessoa() );
                        }
                        
                        movimento.setUsuarioidUsuario( usuarioLogado );
                        movimento.setProdutoidProduto(ctrl.findProduto(idProduto));
                        movimento.setQuantidade(quantidade);
                        movimento.setValorUnitario(valorUnitario);
                        movimento.setTipo(comando);

                        ctrlMov.create(movimento);

                        Produto produto = ctrl.findProduto(idProduto);
                        
                        if( "E".equals(comando) ) produto.setQuantidade(produto.getQuantidade() + quantidade);
                        if( "S".equals(comando) ) produto.setQuantidade(produto.getQuantidade() - quantidade);
                       
                        ctrl.edit(produto);

                        out.writeObject( "E".equals(comando) ? "ENTRADA": "SAIDA" + " registrada com sucesso!");

                    }

                    default ->
                        enviarMensagemCliente(out, "Comando invalido! Favor tentar novamente");
                }

            }

        } catch (IOException ex) {
            try {
                System.out.println("ERRO!");
                s1.close();
            } catch (IOException ex1) {
                Logger.getLogger(CadastroThreadV2.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(CadastroThreadV2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(CadastroThreadV2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Usuario BuscarUsuario(String login, String senha) {
        List<Usuario> listaUsuario = ctrlUsu.findUsuarioEntities();
        return listaUsuario.stream().filter(usuario -> login.equals(usuario.getLogin()) && senha.equals(usuario.getSenha())).findFirst().get();
    }

    private void enviarMensagemCliente(ObjectOutputStream out, String mensagem) throws IOException {
        out.writeObject(mensagem);
        out.flush();
    }

    private String listarProdutos() {
        String newLine = System.getProperty("line.separator");

        List<Produto> listaProdutos = ctrl.findProdutoEntities();

        return listaProdutos.stream().map(produto
                -> produto.getNome() + "::" + produto.getQuantidade()
        ).collect(Collectors.joining(newLine));

    }
}
