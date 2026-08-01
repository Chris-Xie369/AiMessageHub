import UIKit

final class ShareViewController: UIViewController {
    private let suggestionProvider = SuggestionProvider()
    private let inputTextView = UITextView()
    private let resultLabel = UILabel()
    private let stackView = UIStackView()
    private var inputText = ""

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .systemBackground
        setupLayout()
        extractSharedText()
    }

    private func setupLayout() {
        let title = UILabel()
        title.text = "AI 消息中枢"
        title.font = .preferredFont(forTextStyle: .headline)

        inputTextView.heightAnchor.constraint(equalToConstant: 120).isActive = true
        inputTextView.layer.borderWidth = 1
        inputTextView.layer.borderColor = UIColor.separator.cgColor
        inputTextView.layer.cornerRadius = 8

        resultLabel.numberOfLines = 0
        resultLabel.font = .preferredFont(forTextStyle: .body)

        let generateButton = UIButton(type: .system)
        generateButton.setTitle("生成建议", for: .normal)
        generateButton.addTarget(self, action: #selector(generate), for: .touchUpInside)

        stackView.axis = .vertical
        stackView.spacing = 12
        stackView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(stackView)
        stackView.addArrangedSubview(title)
        stackView.addArrangedSubview(inputTextView)
        stackView.addArrangedSubview(generateButton)
        stackView.addArrangedSubview(resultLabel)

        NSLayoutConstraint.activate([
            stackView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            stackView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            stackView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 20),
        ])
    }

    private func extractSharedText() {
        guard
            let item = extensionContext?.inputItems.first as? NSExtensionItem,
            let provider = item.attachments?.first
        else {
            return
        }
        provider.loadItem(forTypeIdentifier: "public.text", options: nil) { [weak self] item, _ in
            DispatchQueue.main.async {
                self?.inputText = item as? String ?? ""
                self?.inputTextView.text = self?.inputText
            }
        }
    }

    @objc private func generate() {
        let text = inputTextView.text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !text.isEmpty else { return }
        let message = ChatMessage(
            app: "share",
            conversationId: "share",
            contact: "分享文本",
            text: text
        )
        Task { @MainActor in
            let result = await suggestionProvider.suggest(
                contact: "分享文本",
                history: [message],
                settings: AppConfig.settings
            )
            render(result)
        }
    }

    private func render(_ result: SuggestionResult) {
        stackView.arrangedSubviews
            .filter { $0 !== inputTextView && $0 !== resultLabel }
            .forEach { $0.removeFromSuperview() }

        if let error = result.error {
            resultLabel.text = error
            resultLabel.textColor = .systemRed
            return
        }
        resultLabel.text = "\(result.variants.count) 条建议"
        resultLabel.textColor = .label

        for variant in result.variants {
            let button = UIButton(type: .system)
            button.setTitle(variant, for: .normal)
            button.titleLabel?.numberOfLines = 0
            button.contentHorizontalAlignment = .leading
            button.addTarget(self, action: #selector(copyVariant(_:)), for: .touchUpInside)
            stackView.addArrangedSubview(button)
        }
    }

    @objc private func copyVariant(_ sender: UIButton) {
        UIPasteboard.general.string = sender.titleLabel?.text
        extensionContext?.completeRequest(returningItems: nil)
    }
}

